package dev.thatsmybaby.survival.listener;

import dev.thatsmybaby.survival.factory.TeleportRequestFactory;
import dev.thatsmybaby.survival.provider.MongoHandler;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import dev.thatsmybaby.survival.provider.RedisProvider;
import dev.thatsmybaby.survival.util.TaskUtils;

public final class PlayerJoinListener implements Listener {

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent ev) {
        Player player = ev.getPlayer();

        player.getInventory().clear();
        player.getEnderChest().clear();
        player.setLevel(0);

        TaskUtils.runAsyncLater(() -> {
            RedisProvider.getInstance().registerPlayer(player.getUniqueId(), player.getName());

            MongoHandler.getInstance().loadPlayerStatistics(player, null);
        },20L);

        // Stuff about Home and Warps
        TaskUtils.runAsync(() -> {
            MongoHandler.getInstance().loadPlayerSync(player.getUniqueId(), player.getName());

            Location pendingLocation = TeleportRequestFactory.getInstance().requestPendingLocation(player.getUniqueId());

            if (pendingLocation == null) {
                pendingLocation = TeleportRequestFactory.getInstance().requestAwaitingLocation(player.getUniqueId());
            }

            if (pendingLocation != null) {
                Location finalPendingLocation = pendingLocation;
                TaskUtils.run(() -> player.teleportAsync(finalPendingLocation));
            }
        });
    }
}