package dev.thatsmybaby.survival.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import dev.thatsmybaby.survival.Placeholders;
import dev.thatsmybaby.survival.Survival;
import dev.thatsmybaby.survival.factory.TeleportRequestFactory;
import dev.thatsmybaby.survival.provider.RedisProvider;
import dev.thatsmybaby.survival.util.TaskUtils;

import java.util.UUID;

public final class TeleportCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(ChatColor.RED + "Run this command in-game");

            return false;
        }

        if (args.length == 0) {
            commandSender.sendMessage(ChatColor.RED + "Usage: /tp <player>");

            return false;
        }

        if (!commandSender.hasPermission("teleport.command.admin")) {
            commandSender.sendMessage(ChatColor.RED + "You don't have permissions to use this command.");

            return false;
        }

        Player target = Bukkit.getPlayer(args[0]);

        if (target == null) {
            TaskUtils.runAsync(() -> this.handleAsync((Player) commandSender, args[0]));

            return false;
        }

        ((Player) commandSender).teleport(target);

        return false;
    }

    private void handleAsync(Player player, String targetName) {
        UUID targetUuidParsed = RedisProvider.getInstance().getTargetPlayer(targetName);

        if (targetUuidParsed == null) {
            player.sendMessage(Placeholders.replacePlaceholders("PLAYER_NOT_FOUND", targetName));

            return;
        }

        if (Survival.released() && targetUuidParsed.equals(player.getUniqueId())) {
            player.sendMessage(Placeholders.replacePlaceholders("YOU_CANT_USE_THIS_ON_YOURSELF"));

            return;
        }

        String targetServerName = RedisProvider.getInstance().getTargetServer(targetUuidParsed);

        if (targetServerName == null || targetServerName.equals(Survival.getServerName())) {
            player.sendMessage(Placeholders.replacePlaceholders("PLAYER_NOT_FOUND", targetName));

            return;
        }

        TeleportRequestFactory.getInstance().addPendingLocationRequest(player.getUniqueId(), targetUuidParsed);

        TaskUtils.run(() -> Survival.connectTo(player, targetServerName));
    }
}