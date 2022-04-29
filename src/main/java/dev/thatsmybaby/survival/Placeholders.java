package dev.thatsmybaby.survival;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Map;

public final class Placeholders {

    private final static Map<String, Object> messages = YamlConfiguration.loadConfiguration(new File(Survival.getInstance().getDataFolder(), "messages.yml")).getValues(true);

    public static String replacePlaceholders(String k, String... args) {
        if (messages.containsKey(k)) {
            k = messages.get(k).toString();
        }

        for (int i = 0; i < args.length; i++) {
            k = k.replaceAll("\\{%" + i + "}", args[i]);
        }

        return ChatColor.translateAlternateColorCodes('&', k);
    }
}