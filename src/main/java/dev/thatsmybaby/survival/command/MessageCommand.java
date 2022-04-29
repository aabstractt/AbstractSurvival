package dev.thatsmybaby.survival.command;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import dev.thatsmybaby.survival.Placeholders;
import dev.thatsmybaby.survival.Survival;
import dev.thatsmybaby.survival.provider.RedisProvider;
import dev.thatsmybaby.survival.provider.redis.CrossServerPrivateMessagePacket;
import dev.thatsmybaby.survival.util.TaskUtils;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class MessageCommand implements CommandExecutor {

    public static Cache<UUID, UUID> lastMessageReceived = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES).build();

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(ChatColor.RED + "Run this command in-game");

            return false;
        }

        if (args.length < 2) {
            commandSender.sendMessage(ChatColor.RED + "Usage: /" + s + " <player> <message>");

            return false;
        }

        String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        Player target = Bukkit.getPlayer(args[0]);

        if (target == null) {
            TaskUtils.runAsync(() -> handleAsync((Player) commandSender, args[0], message));

            return false;
        }

        if (Survival.released() && target.getUniqueId().equals(((Player) commandSender).getUniqueId())) {
            commandSender.sendMessage(Placeholders.replacePlaceholders("YOU_CANT_USE_THIS_ON_YOURSELF"));

            return false;
        }

        lastMessageReceived.put(target.getUniqueId(), ((Player) commandSender).getUniqueId());
        lastMessageReceived.put(((Player) commandSender).getUniqueId(), target.getUniqueId());

        target.sendMessage(Placeholders.replacePlaceholders("PRIVATE_MESSAGE_RECEIVED", commandSender.getName(), message));
        commandSender.sendMessage(Placeholders.replacePlaceholders("PRIVATE_MESSAGE_SENT", target.getName(), message));

        return false;
    }

    private void handleAsync(Player player, String targetName, String message0) {
        UUID targetUuidParsed = RedisProvider.getInstance().getTargetPlayer(targetName);

        if (targetUuidParsed == null) {
            player.sendMessage(Placeholders.replacePlaceholders("PLAYER_NOT_FOUND", targetName));

            return;
        }

        handleAsync(player, targetUuidParsed, message0);
    }

    public static void handleAsync(Player player, UUID finalTargetUuidParsed, String message0) {
        String targetName = RedisProvider.getInstance().getTargetPlayer(finalTargetUuidParsed);

        if (targetName == null) {
            player.sendMessage(Placeholders.replacePlaceholders("PLAYER_NOT_FOUND", "null"));

            return;
        }

        if (Survival.released() && finalTargetUuidParsed.equals(player.getUniqueId())) {
            player.sendMessage(Placeholders.replacePlaceholders("YOU_CANT_USE_THIS_ON_YOURSELF"));

            return;
        }

        RedisProvider.getInstance().redisMessage(new CrossServerPrivateMessagePacket() {{
            this.senderUuidParsed = player.getUniqueId();
            this.targetUuidParsed = finalTargetUuidParsed;

            this.senderName = player.getName();

            this.message = message0;
        }});

        player.sendMessage(Placeholders.replacePlaceholders("PRIVATE_MESSAGE_SENT", targetName, message0));

        lastMessageReceived.put(player.getUniqueId(), finalTargetUuidParsed);
    }
}