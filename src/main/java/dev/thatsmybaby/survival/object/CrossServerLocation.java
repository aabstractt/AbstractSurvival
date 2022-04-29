package dev.thatsmybaby.survival.object;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Location;

@AllArgsConstructor @Getter
public final class CrossServerLocation {

    private String name;
    private String locationSerialized;
    private Location location;
    private String serverName;
}