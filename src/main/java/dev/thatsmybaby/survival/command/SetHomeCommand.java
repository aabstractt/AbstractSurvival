package dev.thatsmybaby.survival.command;

import dev.thatsmybaby.survival.object.CrossServerLocation;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import dev.thatsmybaby.survival.Placeholders;
import dev.thatsmybaby.survival.Survival;
import dev.thatsmybaby.survival.factory.CrossServerTeleportFactory;
import dev.thatsmybaby.survival.object.PlayerSync;
import dev.thatsmybaby.survival.util.TaskUtils;

public final class SetHomeCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(ChatColor.RED + "Run this command in-game");

            return false;
        }

        if (args.length == 0) {
            commandSender.sendMessage(ChatColor.RED + "Usage: /sethome <name>");

            return false;
        }

        PlayerSync playerSync = PlayerSync.of(((Player) commandSender).getUniqueId());

        if (playerSync == null) {
            commandSender.sendMessage(Placeholders.replacePlaceholders("UNEXPECTED_ERROR"));

            return false;
        }

        TaskUtils.runAsync(() -> {
            if (playerSync.getCrossServerLocation(args[0]) == null && Survival.getMaxHomes((Player) commandSender) <= playerSync.getCrossServerLocationMap().size()) {
                commandSender.sendMessage(Placeholders.replacePlaceholders("MAX_HOMES_REACHED", String.valueOf(playerSync.getCrossServerLocationMap().size())));

                return;
            }

            CrossServerLocation crossServerLocation = CrossServerTeleportFactory.getInstance().createPlayerCrossServerLocation(((Player) commandSender).getUniqueId(), args[0], ((Player) commandSender).getLocation());

            if (crossServerLocation == null) {
                commandSender.sendMessage(Placeholders.replacePlaceholders("UNEXPECTED_ERROR"));

                return;
            }

            commandSender.sendMessage(Placeholders.replacePlaceholders("SET_HOME_SUCCESSFULLY_" + (playerSync.getCrossServerLocation(args[0]) == null ? "CREATED" : "UPDATED"), args[0]));

            playerSync.setCrossServerLocation(args[0], crossServerLocation);
        });

        return false;
    }
}