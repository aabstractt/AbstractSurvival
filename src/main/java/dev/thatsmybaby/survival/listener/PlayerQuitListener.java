package dev.thatsmybaby.survival.listener;

import dev.thatsmybaby.survival.factory.TeleportRequestFactory;
import dev.thatsmybaby.survival.object.PlayerSync;
import dev.thatsmybaby.survival.provider.MongoHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import dev.thatsmybaby.survival.provider.RedisProvider;

import java.util.concurrent.CompletableFuture;

public final class PlayerQuitListener implements Listener {

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent ev) {
        Player player = ev.getPlayer();

        PlayerSync playerSync = PlayerSync.of(player.getUniqueId());

        if (playerSync != null) {
            playerSync.clear(player.getUniqueId());
        }

        CompletableFuture.runAsync(() -> {
            MongoHandler.getInstance().updatePlayerStatistics(player);

            RedisProvider.getInstance().unregisterPlayer(player.getUniqueId());
            TeleportRequestFactory.getInstance().removePendingInvitesSent(player.getUniqueId());
        });
    }
}