package dev.thatsmybaby.survival.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import dev.thatsmybaby.survival.provider.redis.RedisMessage;

@RequiredArgsConstructor @Getter
public class RedisPacketReceiveEvent extends Event {

    private static HandlerList handlers = new HandlerList();

    private final RedisMessage packet;

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
