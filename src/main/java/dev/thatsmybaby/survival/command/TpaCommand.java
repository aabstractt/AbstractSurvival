package dev.thatsmybaby.survival.command;

import dev.thatsmybaby.survival.Placeholders;
import dev.thatsmybaby.survival.factory.TeleportRequestFactory;
import dev.thatsmybaby.survival.provider.MongoHandler;
import dev.thatsmybaby.survival.provider.redis.TpaRequestPacket;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import dev.thatsmybaby.survival.Survival;
import dev.thatsmybaby.survival.object.PlayerSync;
import dev.thatsmybaby.survival.provider.RedisProvider;
import dev.thatsmybaby.survival.util.TaskUtils;

import java.util.UUID;

public final class TpaCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(commandSender instanceof Player sender)) {
            commandSender.sendMessage(ChatColor.RED + "Run this command in-game");

            return false;
        }

        if (args.length == 0) {
            commandSender.sendMessage(ChatColor.RED + "Usage: /tpa <player>");

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

            if (TeleportRequestFactory.getInstance().isPendingTpaRequest(sender.getUniqueId(), targetUuidParsed)) {
                sender.sendMessage(Placeholders.replacePlaceholders("TPA_REQUEST_ALREADY_SENT", args[0]));

                return;
            }

            PlayerSync targetSync = MongoHandler.getInstance().loadPlayerSync(targetUuidParsed);

            if (targetSync == null || (Survival.released() && !targetSync.isAcceptingTpaRequests())) {
                sender.sendMessage(Placeholders.replacePlaceholders("TARGET_NOT_IS_ACCEPTING_TPA_REQUESTS"));

                return;
            }

            TeleportRequestFactory.getInstance().addPendingTpaRequest(sender.getUniqueId(), targetUuidParsed);

            Player targetPlayer = Bukkit.getPlayer(targetUuidParsed);

            if (targetPlayer == null) {
                RedisProvider.getInstance().redisMessage(new TpaRequestPacket() {{
                    this.whoReceive = targetUuidParsed;
                    this.whoSent = sender.getName();
                }});
            } else {
                targetPlayer.sendMessage(Placeholders.replacePlaceholders("TPA_REQUEST_RECEIVED", sender.getName()));
            }

            sender.sendMessage(Placeholders.replacePlaceholders("TPA_REQUEST_SUCCESSFULLY_SENT", args[0]));

            BukkitRunnable bukkitRunnable = new BukkitRunnable() {
                @Override
                public void run() {
                    if (!TeleportRequestFactory.getInstance().isPendingTpaRequest(sender.getUniqueId(), targetUuidParsed)) {
                        return;
                    }

                    if (sender.isOnline()) {
                        sender.sendMessage(Placeholders.replacePlaceholders("TPA_REQUEST_SENT_EXPIRED", args[0]));

                        playerSync.removeRunnable(targetUuidParsed);
                    }

                    TeleportRequestFactory.getInstance().removePendingInvite(sender.getUniqueId(), targetUuidParsed);
                }
            };

            bukkitRunnable.runTaskLaterAsynchronously(Survival.getInstance(), Survival.getInstance().getConfig().getInt("general.tpa-timeout", 5) * 20L);

            playerSync.addRunnable(targetUuidParsed, bukkitRunnable);
        });

        return false;
    }
}