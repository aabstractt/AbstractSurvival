package dev.thatsmybaby.survival.util.inventory;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import dev.thatsmybaby.survival.Survival;

import java.util.HashMap;

public abstract class GUIPage implements Listener {

    protected final HashMap<Integer, GUIButton> buttons;
    protected boolean overrideClose = false;
    protected final boolean blockInventoryMovement = true;
    protected final int size;
    protected ClickType type;

    protected final Inventory menu;

    @Getter
    private final Player player;
    private final String name;

    public GUIPage(Player player, String rawName, int size) {
        this.player = player;

        this.size = size;
        this.name = (rawName.length() > 32 ? rawName.substring(0, 32) : rawName);
        this.buttons = new HashMap<>();

        Bukkit.getServer().getPluginManager().registerEvents(this, Survival.getInstance());
        this.menu = Bukkit.getServer().createInventory(null, size, name);

        player.openInventory(this.menu);
    }

    public void build() {
        if (!this.player.isOnline()) {
            destroy();

            return;
        }

        try {
            this.buildPage();

            this.player.updateInventory();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public abstract void buildPage();

    public void addButton(GUIButton button, int slot) {
        if (slot >= size) {
            return;
        }

        if (button.getItem() != null) {
            this.menu.setItem(slot, button.getItem());
        }

        this.buttons.put(slot, button);
    }

    public void removeButton(int slot) {
        this.menu.setItem(slot, null);

        GUIButton button = this.buttons.remove(slot);

        if (button != null) {
            button.destroy();
        }
    }

    public void removeAll() {
        for (int i = 0; i <= size - 1; i++) {
            this.removeButton(i);
        }

        this.buttons.clear();
    }

    public void refresh() {
        this.removeAll();

        this.build();
    }

    public boolean isFree(int slot) {
        return !this.buttons.containsKey(slot);
    }

    public void onInventoryCloseOverride() {

    }

    @EventHandler
    public void onPlayerCloseInventory(InventoryCloseEvent event) {
        if (this.overrideClose) {
            this.onInventoryCloseOverride();

            return;
        }

        Player player = (Player) event.getPlayer();

        if (this.player.getName().equalsIgnoreCase(player.getName())) {
            destroy();

            destroyInternal();
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent ev) {
        Player player = (Player) ev.getWhoClicked();

        this.type = ev.getClick();

        if (!this.player.getName().equalsIgnoreCase(player.getName())) {
            return;
        }

        if (!this.player.getOpenInventory().getTitle().equalsIgnoreCase(name)) {
            return;
        }

        ev.setCancelled(this.blockInventoryMovement);

        GUIButton button = this.buttons.get(ev.getRawSlot());

        if (button == null) {
            return;
        }

        button.click(this);
    }

    public ClickType getType() {
        return type;
    }

    public abstract void destroy();

    public void destroyInternal() {
        HandlerList.unregisterAll(this);

        this.buttons.values().forEach(GUIButton::destroy);

        this.buttons.clear();
    }
}