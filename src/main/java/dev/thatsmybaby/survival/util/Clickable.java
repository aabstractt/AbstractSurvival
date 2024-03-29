package dev.thatsmybaby.survival.util;

import lombok.Getter;
import lombok.NoArgsConstructor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
public class Clickable {

    private List<TextComponent> components = new ArrayList<>();
    private String hoverText;
    private String text;

    public Clickable(String msg) {
        TextComponent message = new TextComponent(ColorUtil.translate(msg));

        this.components.add(message);
        this.text = msg;
    }

    public Clickable(String msg, String hoverMsg, String clickString) {
        this.add(msg, hoverMsg, clickString);
        this.text = msg;
        this.hoverText = hoverMsg;
    }

    public TextComponent add(String msg, String hoverMsg, String clickString) {
        TextComponent message = new TextComponent(ColorUtil.translate(msg));

        if (hoverMsg != null) {
            message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ColorUtil.translate(hoverMsg)).create()));
        }

        if (clickString != null) {
            message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, clickString));
        }

        this.components.add(message);
        this.text = msg;
        this.hoverText = hoverMsg;

        return message;
    }

    public void add(String message) {
        this.components.add(new TextComponent(message));
    }

    public void sendToPlayer(Player player) {
        player.spigot().sendMessage(this.asComponents());
    }

    public void sentToAll() {
        Bukkit.spigot().broadcast(this.asComponents());
    }

    public TextComponent[] asComponents() {
        return this.components.toArray(new TextComponent[0]);
    }
}
