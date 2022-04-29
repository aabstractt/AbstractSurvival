package dev.thatsmybaby.survival.factory;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import dev.thatsmybaby.survival.provider.RedisProvider;
import dev.thatsmybaby.survival.util.LocationUtil;

import java.util.UUID;

public final class TeleportRequestFactory {

    private final static String HASH_PLAYER_TPA_PENDING_REQUESTS = "tpa#pending#requests:%s";
    private final static String HASH_PLAYER_TPA_PENDING_REQUESTS_SENT = "tpa#pending#requests#sent:%s";
    private final static String HASH_AWAITING_PLAYER_LOCATION = "awaiting#location:%s";

    @Getter private final static TeleportRequestFactory instance = new TeleportRequestFactory();

    /**
     * @param whoSent    Who sent the request
     * @param whoAccept Who accept the request
     */
    public void addPendingTpaRequest(UUID whoSent, UUID whoAccept) {
        RedisProvider.execute(jedis -> {
            String hash = String.format(HASH_PLAYER_TPA_PENDING_REQUESTS, whoAccept.toString());

            if (jedis.sismember(hash, whoSent.toString())) {
                return;
            }

            jedis.sadd(hash, whoSent.toString());

            hash = String.format(HASH_PLAYER_TPA_PENDING_REQUESTS_SENT, whoSent);

            if (jedis.sismember(hash, whoAccept.toString())) {
                return;
            }

            jedis.sadd(hash, whoAccept.toString());
            jedis.hset(String.format(HASH_AWAITING_PLAYER_LOCATION, whoAccept), "last_tpa", whoSent.toString());
        });
    }

    public UUID getLastTpaRequest(UUID whoAccept) {
        return RedisProvider.execute(jedis -> {
            String uuidString = jedis.hget(String.format(HASH_AWAITING_PLAYER_LOCATION, whoAccept), "last_tpa");

            if (uuidString == null) {
                return null;
            }

            return UUID.fromString(uuidString);
        });
    }

    /**
     * @param whoSent    Who sent the request
     * @param whoAccept Who accept the request
     */
    public boolean isPendingTpaRequest(UUID whoSent, UUID whoAccept) {
        return RedisProvider.execute(jedis -> jedis.sismember(String.format(HASH_PLAYER_TPA_PENDING_REQUESTS, whoAccept.toString()), whoSent.toString()) && jedis.sismember(String.format(HASH_PLAYER_TPA_PENDING_REQUESTS_SENT, whoSent), whoAccept.toString()));
    }

    /**
     * @param whoSent    Who sent the request
     * @param whoAccept Who accept the request
     */
    public void removePendingInvite(UUID whoSent, UUID whoAccept) {
        RedisProvider.execute(jedis -> {
            String hash = String.format(HASH_PLAYER_TPA_PENDING_REQUESTS, whoAccept.toString());

            if (!jedis.sismember(hash, whoSent.toString())) {
                return;
            }

            jedis.srem(hash, whoSent.toString());

            hash = String.format(HASH_PLAYER_TPA_PENDING_REQUESTS_SENT, whoSent);

            if (!jedis.sismember(hash, whoAccept.toString())) {
                return;
            }

            jedis.srem(hash, whoAccept.toString());
            jedis.hdel(String.format(HASH_AWAITING_PLAYER_LOCATION, whoAccept), "last_tpa");
        });
    }

    public void removePendingInvitesSent(UUID uuidParsed) {
        RedisProvider.execute(jedis -> {
            String hash = String.format(HASH_PLAYER_TPA_PENDING_REQUESTS_SENT, uuidParsed.toString());

            for (String uuidString : jedis.smembers(hash)) {
                jedis.srem(String.format(HASH_PLAYER_TPA_PENDING_REQUESTS, uuidString), uuidParsed.toString());

                jedis.srem(hash, uuidString);
            }
        });
    }

    public void addAwaitingLocation(UUID uuidParsed, String locationString) {
        RedisProvider.execute(jedis -> {
            jedis.hset(String.format(HASH_AWAITING_PLAYER_LOCATION, uuidParsed.toString()), "location", locationString);
        });
    }

    public Location requestAwaitingLocation(UUID uuidParsed) {
        return RedisProvider.execute(jedis -> {
            String hash = String.format(HASH_AWAITING_PLAYER_LOCATION, uuidParsed.toString());

            String locationString = jedis.hget(hash, "location");

            if (locationString == null) {
                return null;
            }

            jedis.hdel(hash, "location");

            return LocationUtil.deserializeLocation(locationString);
        });
    }

    public void addPendingLocationRequest(UUID uuidParsed, UUID targetUuidParsed) {
        RedisProvider.execute(jedis -> {
            jedis.hset(String.format(HASH_AWAITING_PLAYER_LOCATION, uuidParsed.toString()), "request", targetUuidParsed.toString());
        });
    }

    public Location requestPendingLocation(UUID uuidParsed) {
        UUID targetUuidParsed = RedisProvider.execute(jedis -> {
            String hash = String.format(HASH_AWAITING_PLAYER_LOCATION, uuidParsed.toString());

            String uuidString = jedis.hget(hash, "request");

            if (uuidString == null) {
                return null;
            }

            jedis.hdel(hash, "request");

            return UUID.fromString(uuidString);
        });

        if (targetUuidParsed == null) {
            return null;
        }

        Player target = Bukkit.getPlayer(targetUuidParsed);

        return target != null ? target.getLocation() : null;
    }
}