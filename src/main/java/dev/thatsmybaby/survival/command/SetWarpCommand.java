package dev.thatsmybaby.survival.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import dev.thatsmybaby.survival.Placeholders;
import dev.thatsmybaby.survival.factory.CrossServerTeleportFactory;
import dev.thatsmybaby.survival.provider.RedisProvider;
import dev.thatsmybaby.survival.provider.redis.UpdateWarpCrossServerPacket;
import dev.thatsmybaby.survival.util.TaskUtils;

public final class SetWarpCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(ChatColor.RED + "Run this command in-game");

            return false;
        }

        if (args.length == 0) {
            commandSender.sendMessage(ChatColor.RED + "Usage: /setwarp <name>");

            return false;
        }

        if (!commandSender.hasPermission("setwarp.command.admin")) {
            commandSender.sendMessage(ChatColor.RED + "You don't have permissions to use this command.");

            return false;
        }

        commandSender.sendMessage(Placeholders.replacePlaceholders("WARP_SUCCESSFULLY_" + (CrossServerTeleportFactory.getInstance().getWarpCrossServerLocation(args[0]) == null ? "CREATED" : "UPDATED"), args[0]));

        TaskUtils.runAsync(() -> CrossServerTeleportFactory.getInstance().createWarpCrossServerLocation(args[0], ((Player) commandSender).getLocation()));

        TaskUtils.run(() -> RedisProvider.getInstance().redisMessage(new UpdateWarpCrossServerPacket()));

        return false;
    }
}