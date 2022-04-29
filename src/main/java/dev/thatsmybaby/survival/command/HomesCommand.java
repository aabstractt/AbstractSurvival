package dev.thatsmybaby.survival.command;

import dev.thatsmybaby.survival.Placeholders;
import dev.thatsmybaby.survival.factory.CrossServerTeleportFactory;
import dev.thatsmybaby.survival.object.CrossServerLocation;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import dev.thatsmybaby.survival.object.PlayerSync;

import java.util.Map;

public final class HomesCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 0 && !(commandSender instanceof Player)) {
            commandSender.sendMessage(ChatColor.RED + "Usage: /homes <player>");

            return false;
        }

        if (commandSender instanceof Player && (args.length == 0 || !commandSender.hasPermission("homes.command.others"))) {
            PlayerSync playerSync = PlayerSync.of(((Player) commandSender).getUniqueId());

            if (playerSync == null) {
                commandSender.sendMessage(Placeholders.replacePlaceholders("UNEXPECTED_ERROR"));

                return false;
            }

            handleSeeHomes(commandSender, args[0], playerSync.getCrossServerLocationMap());

            return false;
        }

        Map<String, CrossServerLocation> crossServerLocationMap = CrossServerTeleportFactory.getInstance().loadPlayerCrossServerLocation(args[0]);

        if (crossServerLocationMap == null) {
            commandSender.sendMessage(Placeholders.replacePlaceholders("PLAYER_NOT_FOUND", args[0]));

            return false;
        }

        handleSeeHomes(commandSender, args[0], crossServerLocationMap);

        return false;
    }

    private void handleSeeHomes(CommandSender sender, String name, Map<String, CrossServerLocation> crossServerLocationMap) {
        sender.sendMessage(Placeholders.replacePlaceholders("HOME_LIST_PLAYER", name));

        for (CrossServerLocation crossServerLocation : crossServerLocationMap.values()) {
            Location l = crossServerLocation.getLocation();

            sender.sendMessage(Placeholders.replacePlaceholders("HOME_LIST_TEXT", crossServerLocation.getName(), crossServerLocation.getServerName(), String.valueOf(l.getBlockX()), String.valueOf(l.getBlockY()), String.valueOf(l.getBlockZ())));
        }
    }
}