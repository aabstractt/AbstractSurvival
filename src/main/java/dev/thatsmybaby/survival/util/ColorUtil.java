package dev.thatsmybaby.survival.util;

import org.bukkit.ChatColor;

import java.util.List;
import java.util.stream.Collectors;

public class ColorUtil {

    public static String translate(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public static List<String> translate(List<String> s) {
        return s.stream().map(ColorUtil::translate).collect(Collectors.toList());
    }
}
