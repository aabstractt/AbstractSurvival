package dev.thatsmybaby.survival.command;

import dev.thatsmybaby.survival.Placeholders;
import dev.thatsmybaby.survival.factory.CrossServerTeleportFactory;
import dev.thatsmybaby.survival.object.CrossServerLocation;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import dev.thatsmybaby.survival.provider.RedisProvider;
import dev.thatsmybaby.survival.provider.redis.UpdateWarpCrossServerPacket;
import dev.thatsmybaby.survival.util.TaskUtils;

public final class DelWarpCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 0) {
            commandSender.sendMessage(ChatColor.RED + "Usage: /delwarp <name>");

            return false;
        }

        if (!commandSender.hasPermission("delwarp.command.admin")) {
            commandSender.sendMessage(ChatColor.RED + "You don't have permissions to use this command.");

            return false;
        }

        CrossServerLocation crossServerLocation = CrossServerTeleportFactory.getInstance().getWarpCrossServerLocation(args[0]);

        if (crossServerLocation == null) {
            commandSender.sendMessage(Placeholders.replacePlaceholders("WARP_NOT_FOUND", args[0]));

            return false;
        }

        commandSender.sendMessage(Placeholders.replacePlaceholders("WARP_SUCCESSFULLY_DELETED", crossServerLocation.getName()));

        TaskUtils.runAsync(() -> CrossServerTeleportFactory.getInstance().removeWarpCrossServerLocation(crossServerLocation.getName()));
        TaskUtils.run(() -> RedisProvider.getInstance().redisMessage(new UpdateWarpCrossServerPacket()));

        return false;
    }
}