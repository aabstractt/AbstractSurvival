package dev.thatsmybaby.survival.object;

import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;

@AllArgsConstructor
public final class PlayerSync {

    private final static Map<UUID, PlayerSync> playerSyncMap = Maps.newConcurrentMap();

    private final Map<UUID, BukkitRunnable> runnableMap = Maps.newConcurrentMap();

    @Getter private final Map<String, CrossServerLocation> crossServerLocationMap;

    @Getter @Setter private boolean autoPickup;
    @Setter private boolean canSeeChat;
    @Setter private boolean canSeeAnnounces;
    @Setter private boolean canShowWelcome;
    @Getter @Setter private boolean acceptingTpaRequests;
    @Getter @Setter private boolean alreadyTeleporting;

    public CrossServerLocation getCrossServerLocation(String name) {
        return this.crossServerLocationMap.get(name.toLowerCase());
    }

    public void setCrossServerLocation(String name, CrossServerLocation gameHome) {
        this.crossServerLocationMap.put(name.toLowerCase(), gameHome);
    }

    public void removeCrossServerLocation(String name) {
        this.crossServerLocationMap.remove(name.toLowerCase());
    }

    public void addRunnable(UUID targetUuidParsed, BukkitRunnable runnable) {
        this.runnableMap.put(targetUuidParsed, runnable);
    }

    public void removeRunnable(UUID targetUuidParsed) {
        this.runnableMap.remove(targetUuidParsed);
    }

    public boolean canSeeChat() {
        return this.canSeeChat;
    }

    public boolean canSeeAnnounces() {
        return this.canSeeAnnounces;
    }

    public boolean canShowWelcome() {
        return this.canShowWelcome;
    }

    public void cancelRunnable(UUID uuidParsed) {
        BukkitRunnable bukkitRunnable = this.runnableMap.remove(uuidParsed);

        if (bukkitRunnable == null) {
            return;
        }

        bukkitRunnable.cancel();
    }

    public void clear(UUID uuidParsed) {
        this.runnableMap.clear();

        this.crossServerLocationMap.clear();

        playerSyncMap.remove(uuidParsed);
    }

    public static void add(UUID uuidParsed, PlayerSync playerSync) {
        playerSyncMap.put(uuidParsed, playerSync);
    }

    public static PlayerSync of(UUID uuidParsed) {
        return playerSyncMap.get(uuidParsed);
    }
}