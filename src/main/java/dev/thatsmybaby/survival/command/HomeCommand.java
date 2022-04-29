package dev.thatsmybaby.survival.command;

import dev.thatsmybaby.survival.Placeholders;
import dev.thatsmybaby.survival.factory.TeleportRequestFactory;
import dev.thatsmybaby.survival.object.CrossServerLocation;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import dev.thatsmybaby.survival.Survival;
import dev.thatsmybaby.survival.object.PlayerSync;
import dev.thatsmybaby.survival.util.LocationUtil;
import dev.thatsmybaby.survival.util.TaskUtils;

public final class HomeCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(ChatColor.RED + "Run this command in-game");

            return false;
        }

        if (args.length == 0) {
            commandSender.sendMessage(ChatColor.RED + "Usage: /home <name>");

            return false;
        }

        PlayerSync playerSync = PlayerSync.of(((Player) commandSender).getUniqueId());

        if (playerSync == null) {
            commandSender.sendMessage(Placeholders.replacePlaceholders("UNEXPECTED_ERROR"));

            return false;
        }

        if (playerSync.isAlreadyTeleporting()) {
            commandSender.sendMessage(Placeholders.replacePlaceholders("HOME_ALREADY_TELEPORTING"));

            return false;
        }

        CrossServerLocation crossServerLocation = playerSync.getCrossServerLocation(args[0]);

        if (crossServerLocation == null) {
            commandSender.sendMessage(Placeholders.replacePlaceholders("HOME_NOT_FOUND", args[0]));

            return false;
        }

        doTeleportQueue((Player) commandSender, playerSync, crossServerLocation.getLocationSerialized(), crossServerLocation.getServerName());

        return false;
    }

    public static void doTeleportQueue(Player player, PlayerSync playerSync, String locationString, String serverName) {
        playerSync.setAlreadyTeleporting(true);

        Location initialLocation = player.getLocation();

        final int[] time = {Survival.getInstance().getSettings().getInt("general.teleport_time") + 1};

        new BukkitRunnable() {

            @Override
            public void run() {
                if (!playerSync.isAlreadyTeleporting()) {
                    cancel();

                    return;
                }

                if (hasMoved(player.getLocation(), initialLocation)) {
                    player.sendMessage(Placeholders.replacePlaceholders("TELEPORTING_CANCELLED_MOVEMENT"));

                    playerSync.setAlreadyTeleporting(false);

                    return;
                }

                if (!player.isOnline()) {
                    playerSync.setAlreadyTeleporting(false);

                    return;
                }

                time[0]--;

                if (time[0] > 0) {
                    player.sendMessage(Placeholders.replacePlaceholders("TELEPORTING_COUNTDOWN", Integer.toString(time[0])));

                    return;
                }

                playerSync.setAlreadyTeleporting(false);

                if (!serverName.equals(Survival.getServerName())) {
                    TeleportRequestFactory.getInstance().addAwaitingLocation(player.getUniqueId(), locationString);

                    Bukkit.getScheduler().runTask(Survival.getInstance(), () -> Survival.connectTo(player, serverName));

                    return;
                }

                TaskUtils.run(() -> player.teleportAsync(LocationUtil.deserializeLocation(locationString)));
            }
        }.runTaskTimer(Survival.getInstance(), 0, 20L);
    }

    // This returns if the player has moved during a timed teleport
    private static boolean hasMoved(Location location, Location initialLocation) {
        double xDiff = makePositive(initialLocation.getX() - location.getX());
        double yDiff = makePositive(initialLocation.getY() - location.getY());
        double zDiff = makePositive(initialLocation.getZ() - location.getZ());

        return (xDiff + yDiff + zDiff) > 0.1;
    }

    // This converts a negative to a positive double, used in checking if a player has moved
    private static double makePositive(double d) {
        if (d < 0) {
            d = d * -1D;
        }
        return d;
    }
}