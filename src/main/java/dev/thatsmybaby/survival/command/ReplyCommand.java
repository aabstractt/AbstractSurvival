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
import dev.thatsmybaby.survival.util.TaskUtils;

import java.util.Arrays;
import java.util.UUID;

public final class ReplyCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(ChatColor.RED + "Run this command in-game");

            return false;
        }

        UUID targetUuidParsed = MessageCommand.lastMessageReceived.getIfPresent(((Player) commandSender).getUniqueId());

        if (targetUuidParsed == null) {
            commandSender.sendMessage(Placeholders.replacePlaceholders("PRIVATE_MESSAGE_PENDING_NOT_FOUND"));

            return false;
        }

        String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        Player target = Bukkit.getPlayer(targetUuidParsed);

        if (target == null) {
            TaskUtils.runAsync(() -> MessageCommand.handleAsync((Player) commandSender, targetUuidParsed, message));

            return false;
        }

        if (Survival.released() && target.getUniqueId().equals(((Player) commandSender).getUniqueId())) {
            commandSender.sendMessage(Placeholders.replacePlaceholders("YOU_CANT_USE_THIS_ON_YOURSELF"));

            return false;
        }

        MessageCommand.lastMessageReceived.put(target.getUniqueId(), ((Player) commandSender).getUniqueId());
        MessageCommand.lastMessageReceived.put(((Player) commandSender).getUniqueId(), target.getUniqueId());

        target.sendMessage(Placeholders.replacePlaceholders("PRIVATE_MESSAGE_RECEIVED", commandSender.getName(), message));
        commandSender.sendMessage(Placeholders.replacePlaceholders("PRIVATE_MESSAGE_SENT", target.getName(), message));

        return false;
    }
}