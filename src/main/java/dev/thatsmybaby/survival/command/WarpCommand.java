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

public final class WarpCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(ChatColor.RED + "Run this command in-game");

            return false;
        }

        if (args.length == 0) {
            commandSender.sendMessage(ChatColor.RED + "Usage: /warp <name>");

            return false;
        }

        PlayerSync playerSync = PlayerSync.of(((Player) commandSender).getUniqueId());

        if (playerSync == null) {
            commandSender.sendMessage(Placeholders.replacePlaceholders("UNEXPECTED_ERROR"));

            return false;
        }

        if (playerSync.isAlreadyTeleporting()) {
            commandSender.sendMessage(Placeholders.replacePlaceholders("PLAYER_ALREADY_TELEPORTING"));

            return false;
        }

        CrossServerLocation crossServerLocation = CrossServerTeleportFactory.getInstance().getWarpCrossServerLocation(args[0]);

        if (crossServerLocation == null) {
            commandSender.sendMessage(Placeholders.replacePlaceholders("WARP_NOT_FOUND", args[0]));

            return false;
        }

        if (!commandSender.hasPermission("warp." + crossServerLocation.getName().toLowerCase())) {
            commandSender.sendMessage(Placeholders.replacePlaceholders("NO_ENOUGH_PERMS_WARP", crossServerLocation.getName()));

            return false;
        }

        HomeCommand.doTeleportQueue((Player) commandSender, playerSync, crossServerLocation.getLocationSerialized(), crossServerLocation.getServerName());

        return false;
    }
}