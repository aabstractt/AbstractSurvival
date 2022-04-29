package dev.thatsmybaby.survival.listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import dev.thatsmybaby.survival.Survival;
import dev.thatsmybaby.survival.provider.RedisProvider;
import dev.thatsmybaby.survival.provider.redis.PlayerMessagePacket;

public final class PlayerChatListener implements Listener {

    @EventHandler
    public void onAsyncChatEvent(AsyncChatEvent ev) {
        Player player = ev.getPlayer();

        ev.setCancelled(true);

        TextComponent textComponent = (TextComponent) ev.message();

        RedisProvider.getInstance().redisMessage(new PlayerMessagePacket() {{
            this.playerName = ((TextComponent) player.displayName()).content();
            this.message = textComponent.content();

            this.serverName = Survival.getServerName();
        }});
    }
}