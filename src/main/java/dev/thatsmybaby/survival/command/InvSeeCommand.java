package dev.thatsmybaby.survival.command;

import dev.thatsmybaby.survival.Placeholders;
import dev.thatsmybaby.survival.util.InvSeeMenu;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class InvSeeCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(ChatColor.RED + "Run this command in-game");

            return false;
        }

        if (!commandSender.hasPermission("invsee.command")) {
            commandSender.sendMessage(ChatColor.RED + "You don't have permissions to use this command.");

            return false;
        }

        if (args.length == 0) {
            commandSender.sendMessage(ChatColor.RED + "Usage: /invsee <player>");

            return false;
        }

        Player target = Bukkit.getPlayer(args[0]);

        if (target == null) {
            commandSender.sendMessage(Placeholders.replacePlaceholders("PLAYER_NOT_FOUND", args[0]));

            return false;
        }

        InvSeeMenu invSeeMenu = new InvSeeMenu((Player) commandSender, target, target.getName() + "'s inventory", 54);
        invSeeMenu.build();

        return false;
    }
}