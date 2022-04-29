package dev.thatsmybaby.survival.command;

import dev.thatsmybaby.survival.Placeholders;
import dev.thatsmybaby.survival.factory.CrossServerTeleportFactory;
import dev.thatsmybaby.survival.object.CrossServerLocation;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import dev.thatsmybaby.survival.object.PlayerSync;
import dev.thatsmybaby.survival.util.TaskUtils;

public final class DelHomeCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(ChatColor.RED + "Run this command in-game");

            return false;
        }

        if (args.length == 0) {
            commandSender.sendMessage(ChatColor.RED + "Usage: /delhome <name>");

            return false;
        }

        PlayerSync playerSync = PlayerSync.of(((Player) commandSender).getUniqueId());

        if (playerSync == null) {
            commandSender.sendMessage(Placeholders.replacePlaceholders("UNEXPECTED_ERROR"));

            return false;
        }

        CrossServerLocation crossServerLocation = playerSync.getCrossServerLocation(args[0]);

        if (crossServerLocation == null) {
            commandSender.sendMessage(Placeholders.replacePlaceholders("HOME_NOT_FOUND", args[0]));

            return false;
        }

        commandSender.sendMessage(Placeholders.replacePlaceholders("HOME_SUCCESSFULLY_DELETED", crossServerLocation.getName()));

        playerSync.removeCrossServerLocation(args[0]);
        TaskUtils.runAsync(() -> CrossServerTeleportFactory.getInstance().removePlayerCrossServerLocation(((Player) commandSender).getUniqueId(), crossServerLocation.getName()));

        return false;
    }
}