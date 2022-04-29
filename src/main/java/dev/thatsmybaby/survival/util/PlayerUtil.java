package dev.thatsmybaby.survival.util;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

public class PlayerUtil {

    public static PotionEffect[] getPlayerPotionEffects(Player player) {
        PotionEffect[] potionEffects = new PotionEffect[player.getActivePotionEffects().size()];
        int arrayIndex = 0;
        for (PotionEffect effect : player.getActivePotionEffects()) {
            potionEffects[arrayIndex] = effect;
            arrayIndex++;
        }
        return potionEffects;
    }

    public static void setPlayerPotionEffects(Player player, PotionEffect[] effects) {
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
        for (PotionEffect effect : effects) {
            player.addPotionEffect(effect);
        }
    }
}
