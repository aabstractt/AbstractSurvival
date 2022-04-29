package dev.thatsmybaby.survival.util;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import dev.thatsmybaby.survival.Survival;
import dev.thatsmybaby.survival.util.inventory.GUIPage;
import dev.thatsmybaby.survival.util.inventory.util.ItemStackBuilder;

public final class InvSeeMenu extends GUIPage {

    private Player target;
    private BukkitTask runnable;

    public InvSeeMenu(Player player, Player target, String rawName, int size) {
        super(player, rawName, size);

        this.target = target;
    }

    @Override
    public void buildPage() {
        this.runnable = new BukkitRunnable() {
            @Override
            public void run() {
                if (target == null || !target.isOnline()) {
                    this.cancel();
                    return;
                }

                ItemStack[] armor = target.getInventory().getArmorContents();
                ItemStack[] contents = target.getInventory().getContents();

                menu.setContents(contents);

                menu.setItem(52, armor[0]);
                menu.setItem(50, armor[1]);
                menu.setItem(48, armor[2]);
                menu.setItem(46, armor[3]);

                menu.setItem(36, createGlass());
                menu.setItem(37, createGlass());
                menu.setItem(38, createGlass());
                menu.setItem(39, createGlass());
                menu.setItem(40, new ItemStackBuilder(Material.EXPERIENCE_BOTTLE).setName("§eXP: §f" + target.getLevel()));
                menu.setItem(41, createGlass());
                menu.setItem(42, createGlass());
                menu.setItem(43, createGlass());
                menu.setItem(44, createGlass());
                menu.setItem(45, createGlass());
                menu.setItem(47, createGlass());
                menu.setItem(49, createGlass());
                menu.setItem(51, createGlass());
                menu.setItem(53, createGlass());
            }
        }.runTaskTimerAsynchronously(Survival.getInstance(), 0L, 20L);
    }

    public ItemStack createGlass() {
        ItemStack item = new ItemStack(Material.RED_STAINED_GLASS_PANE, 1);
        ItemMeta itemmeta = item.getItemMeta();
        item.setItemMeta(itemmeta);
        return item;
    }

    @Override
    public void destroy() {
        this.runnable.cancel();
    }
}