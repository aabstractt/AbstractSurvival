package dev.thatsmybaby.survival.command;

import dev.thatsmybaby.survival.Placeholders;
import dev.thatsmybaby.survival.factory.TeleportRequestFactory;
import dev.thatsmybaby.survival.object.PlayerSync;
import dev.thatsmybaby.survival.provider.MongoHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import dev.thatsmybaby.survival.Survival;
import dev.thatsmybaby.survival.provider.RedisProvider;
import dev.thatsmybaby.survival.provider.redis.TpaDenyRequestPacket;
import dev.thatsmybaby.survival.util.TaskUtils;

import java.util.UUID;

public final class TpaDenyCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(commandSender instanceof Player sender)) {
            commandSender.sendMessage(ChatColor.RED + "Run this command in-game");

            return false;
        }

        if (args.length == 0) {
            commandSender.sendMessage(ChatColor.RED + "Usage: /tpadeny <player>");

            return false;
        }

        PlayerSync playerSync = PlayerSync.of(sender.getUniqueId());

        if (playerSync == null) {
            commandSender.sendMessage(Placeholders.replacePlaceholders("UNEXPECTED_ERROR"));

            return false;
        }

        if (playerSync.isAlreadyTeleporting()) {
            commandSender.sendMessage(Placeholders.replacePlaceholders("PLAYER_ALREADY_TELEPORTING"));

            return false;
        }

        if (Survival.released() && !playerSync.isAcceptingTpaRequests()) {
            commandSender.sendMessage(Placeholders.replacePlaceholders("YOU_NOT_ARE_ACCEPTING_TPA_REQUESTS"));

            return false;
        }

        TaskUtils.runAsync(() -> {
            UUID targetUuidParsed = RedisProvider.getInstance().getTargetPlayer(args[0]);

            if (targetUuidParsed == null) {
                sender.sendMessage(Placeholders.replacePlaceholders("PLAYER_NOT_FOUND", args[0]));

                return;
            }

            if (Survival.released() && targetUuidParsed.equals(sender.getUniqueId())) {
                commandSender.sendMessage(Placeholders.replacePlaceholders("YOU_CANT_USE_THIS_ON_YOURSELF"));

                return;
            }

            if (!TeleportRequestFactory.getInstance().isPendingTpaRequest(targetUuidParsed, sender.getUniqueId())) {
                sender.sendMessage(Placeholders.replacePlaceholders("NO_PENDING_TPA_REQUEST", args[0]));

                return;
            }

            PlayerSync targetSync = MongoHandler.getInstance().loadPlayerSync(targetUuidParsed);

            if (targetSync == null || (Survival.released() && !targetSync.isAcceptingTpaRequests())) {
                sender.sendMessage(Placeholders.replacePlaceholders("TARGET_NOT_IS_ACCEPTING_TPA_REQUESTS"));

                return;
            }

            TeleportRequestFactory.getInstance().removePendingInvite(targetUuidParsed, sender.getUniqueId());

            Player targetPlayer = Bukkit.getPlayer(targetUuidParsed);

            if (targetPlayer == null) {
                RedisProvider.getInstance().redisMessage(new TpaDenyRequestPacket() {{
                    this.whoDeny = sender.getName();

                    this.whoReceive = targetUuidParsed;
                }});
            } else {
                targetPlayer.sendMessage(Placeholders.replacePlaceholders("TARGET_TPA_REQUEST_DENIED", sender.getName()));
            }

            targetSync.cancelRunnable(sender.getUniqueId());

            sender.sendMessage(Placeholders.replacePlaceholders("TPA_REQUEST_SUCCESSFULLY_DENIED", args[0]));
        });

        return false;
    }
}