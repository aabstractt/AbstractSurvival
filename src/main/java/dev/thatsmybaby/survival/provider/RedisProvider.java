package dev.thatsmybaby.survival.provider;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import dev.thatsmybaby.survival.Survival;
import dev.thatsmybaby.survival.factory.TeleportRequestFactory;
import dev.thatsmybaby.survival.provider.redis.*;
import lombok.Getter;
import org.bukkit.Bukkit;
import redis.clients.jedis.*;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings("UnstableApiUsage")
public final class RedisProvider {

    @Getter private final static RedisProvider instance = new RedisProvider();

    private static final Map<Integer, Class<? extends RedisMessage>> messagesPool = new HashMap<>();

    private JedisPool jedisPool = null;
    private Subscription jedisPubSub = null;

    private String password = null;

    public void init(String address, String password) {
        if (address == null) {
            return;
        }

        String[] addressSplit = address.split(":");
        String host = addressSplit[0];
        int port = addressSplit.length > 1 ? Integer.parseInt(addressSplit[1]) : Protocol.DEFAULT_PORT;

        this.password = password != null && password.length() > 0 ? password : null;

        this.jedisPool = new JedisPool(new JedisPoolConfig(), host, port, 30_000, this.password, 0, null);

        registerMessage(
                new PlayerMessagePacket(),
                new TpaRequestPacket(),
                new TpaAcceptRequestPacket(),
                new TpaDenyRequestPacket(),
                new CrossServerPrivateMessagePacket(),
                new UpdateWarpCrossServerPacket()
        );

        ForkJoinPool.commonPool().execute(() -> execute(jedis -> {
            jedis.subscribe(this.jedisPubSub = new Subscription(), "SurvivalSync".getBytes(StandardCharsets.UTF_8));
        }));

        if (this.connected()) {
            Bukkit.getLogger().info("Redis is now successfully connected and synchronization is ready!");
        } else {
            Bukkit.getLogger().info("Could not connect to redis, synchronization won't work with other servers, you will still be able to use VicnixCore normally on this server. (" + Bukkit.getName() + ")");
        }
    }

    public void registerPlayer(UUID uuidParsed, String name) {
        execute(jedis -> {
            if (!jedis.hexists("uuid-cache", name.toLowerCase())) {
                jedis.hset("uuid-cache", name.toLowerCase(), uuidParsed.toString());
            }

            if (!jedis.hexists("usernames-cache", uuidParsed.toString())) {
                jedis.hset("usernames-cache", uuidParsed.toString(), name);
            }

            jedis.hset("server-cache", uuidParsed.toString(), Survival.getServerName());
            jedis.sadd("players-cache:" + Survival.getServerName(), uuidParsed.toString());
            jedis.sadd("players-online", name);
        });
    }

    public void unregisterPlayer(UUID uuidParsed) {
        execute(jedis -> {
            String targetName = this.getTargetPlayer(uuidParsed);

            if (targetName != null) {
                jedis.hdel("usernames-cache", uuidParsed.toString());
                jedis.hdel("uuid-cache", targetName.toLowerCase());

                jedis.srem("players-online", targetName);
            }

            jedis.hdel("server-cache", uuidParsed.toString());
            jedis.srem("players-cache:" + Survival.getServerName(), uuidParsed.toString());
        });
    }

    public UUID getTargetPlayer(String name) {
        return execute(jedis -> {
            String uuidString = jedis.hget("uuid-cache", name.toLowerCase());

            if (uuidString == null) {
                return null;
            }

            return UUID.fromString(uuidString);
        });
    }

    public String getTargetPlayer(UUID uuidParsed) {
        return execute(jedis -> {
            return jedis.hget("usernames-cache", uuidParsed.toString());
        });
    }

    public String getTargetServer(UUID uuidParsed) {
        return execute(jedis -> {
            return jedis.hget("server-cache", uuidParsed.toString());
        });
    }

    public void redisMessage(RedisMessage pk) {
        CompletableFuture.runAsync(() -> execute(jedis -> {
            ByteArrayDataOutput stream = ByteStreams.newDataOutput();

            stream.writeInt(pk.getId());
            pk.encode(stream);

            jedis.publish("SurvivalSync".getBytes(StandardCharsets.UTF_8), stream.toByteArray());
        }));
    }

    public static <T> T execute(Function<Jedis, T> action) {
        if (!instance.connected()) {
            throw new RuntimeException("Jedis was disconnected");
        }

        try (Jedis jedis = instance.jedisPool.getResource()) {
            if (instance.password != null && !instance.password.isEmpty()) {
                jedis.auth(instance.password);
            }

            return action.apply(jedis);
        }
    }

    public static void execute(Consumer<Jedis> action) {
        if (!instance.connected()) {
            return;
        }

        try (Jedis jedis = instance.jedisPool.getResource()) {
            if (instance.password != null && !instance.password.isEmpty()) {
                jedis.auth(instance.password);
            }

            action.accept(jedis);
        }
    }

    public boolean connected() {
        return this.jedisPool != null && !this.jedisPool.isClosed();
    }

    public void close() throws InterruptedException {
        if (this.jedisPubSub != null) {
            this.jedisPubSub.unsubscribe();
        }

        execute(jedis -> {
            for (String uuidString : jedis.smembers("players-cache:" + Survival.getServerName())) {
                UUID targetUuidParsed = UUID.fromString(uuidString);

                this.unregisterPlayer(targetUuidParsed);
                TeleportRequestFactory.getInstance().removePendingInvitesSent(targetUuidParsed);
            }
        });

        // Thread Sleep to allow remove players
        Thread.sleep(1000);

        if (this.jedisPool != null) {
            this.jedisPool.destroy();
        }
    }

    public void registerMessage(RedisMessage... pools) {
        for (RedisMessage pool : pools) {
            messagesPool.put(pool.getId(), pool.getClass());
        }
    }

    private RedisMessage constructMessage(int pid) {
        Class<? extends RedisMessage> instance = messagesPool.get(pid);

        if (instance == null) {
            return null;
        }

        try {
            return instance.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    protected class Subscription extends BinaryJedisPubSub {

        @Override
        public void onMessage(byte[] channel, byte[] message) {
            ByteArrayDataInput stream = ByteStreams.newDataInput(message);

            RedisMessage pk = constructMessage(stream.readInt());

            if (pk == null) {
                Survival.getInstance().getLogger().warning("Redis packet received is null");

                return;
            }

            pk.decode(stream);

            if (pk.isAsync()) {
                CompletableFuture.runAsync(pk::handle);
            } else {
                pk.handle();
            }

            Survival.getInstance().getLogger().info("Packet " + pk.getClass().getName() + " decoded and handled!");
        }
    }
}