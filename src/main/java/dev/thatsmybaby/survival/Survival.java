package dev.thatsmybaby.survival;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import dev.thatsmybaby.survival.command.*;
import dev.thatsmybaby.survival.factory.CrossServerTeleportFactory;
import dev.thatsmybaby.survival.listener.AsyncTabCompleteListener;
import dev.thatsmybaby.survival.listener.PlayerChatListener;
import dev.thatsmybaby.survival.listener.PlayerJoinListener;
import dev.thatsmybaby.survival.provider.MongoHandler;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import dev.thatsmybaby.survival.listener.PlayerQuitListener;
import dev.thatsmybaby.survival.provider.RedisProvider;
import dev.thatsmybaby.survival.util.ConfigUtil;

@Getter @SuppressWarnings( "UnstableApiUsage")
public class Survival extends JavaPlugin {

    @Getter private static Survival instance;

    private ConfigUtil settings;

    @SneakyThrows @Override
    public void onEnable() {
        instance = this;

        handleConfigs();

        // I use try to check if the command exists if not exists going to disable the plugin
        try {
            this.getCommandNonNullable("sethome").setExecutor(new SetHomeCommand());
            this.getCommandNonNullable("delhome").setExecutor(new DelHomeCommand());
            this.getCommandNonNullable("homes").setExecutor(new HomesCommand());
            this.getCommandNonNullable("home").setExecutor(new HomeCommand());

            this.getCommandNonNullable("setwarp").setExecutor(new SetWarpCommand());
            this.getCommandNonNullable("delwarp").setExecutor(new DelWarpCommand());
            this.getCommandNonNullable("warp").setExecutor(new WarpCommand());

            this.getCommandNonNullable("tpa").setExecutor(new TpaCommand());
            this.getCommandNonNullable("tpaccept").setExecutor(new TpaAcceptCommand());
            this.getCommandNonNullable("tpadeny").setExecutor(new TpaDenyCommand());
            this.getCommandNonNullable("tp").setExecutor(new TeleportCommand());

            this.getCommandNonNullable("message").setExecutor(new MessageCommand());
            this.getCommandNonNullable("reply").setExecutor(new ReplyCommand());

            this.getCommandNonNullable("invsee").setExecutor(new InvSeeCommand());
        } catch (RuntimeException e) {
            getLogger().warning(e.getMessage());

            getPluginLoader().disablePlugin(this);
            getServer().getPluginManager().disablePlugin(this);

            return;
        }

        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        MongoHandler.getInstance().connect();
        RedisProvider.getInstance().init(this.getSettings().getString("redis.host"), this.getSettings().getString("redis.password"));

        CrossServerTeleportFactory.getInstance().loadWarpCrossServerLocations();

        this.getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
        this.getServer().getPluginManager().registerEvents(new PlayerQuitListener(), this);
        this.getServer().getPluginManager().registerEvents(new PlayerChatListener(), this);
        this.getServer().getPluginManager().registerEvents(new AsyncTabCompleteListener(), this);
    }

    public static String getServerName() {
        return instance.getSettings().getString("server-name");
    }

    @Override
    public void onDisable() {
        try {
            RedisProvider.getInstance().close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void handleConfigs() {
        this.settings = new ConfigUtil(this, "Settings.yml");

        this.saveResource("messages.yml", false);
    }

    public static void connectTo(Player player, String serverName) {
        ByteArrayDataOutput stream = ByteStreams.newDataOutput();

        stream.writeUTF("Connect");
        stream.writeUTF(serverName);

        player.sendPluginMessage(Survival.getInstance(), "BungeeCord", stream.toByteArray());
    }

    private PluginCommand getCommandNonNullable(String commandName) {
        PluginCommand command = getCommand(commandName);

        if (command == null) {
            throw new RuntimeException("Command not found");
        }

        return command;
    }

    public static int getMaxHomes(Player player) {
        for (int i = 50; i > 0; i--) {
            if (!player.hasPermission("survivalcore.homes." + i)) {
                continue;
            }

            return i;
        }

        return 0;
    }

    public static boolean released() {
        return false;
    }
}