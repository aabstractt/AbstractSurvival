package dev.thatsmybaby.survival.provider.redis;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import dev.thatsmybaby.survival.Survival;

public class PlayerMessagePacket extends RedisMessage {

    public String playerName;

    public String message;

    public String serverName;

    public PlayerMessagePacket() {
        super(0);
    }

    @Override
    public void decode(ByteArrayDataInput stream) {
        this.playerName = stream.readUTF();

        this.message = stream.readUTF();

        this.serverName = stream.readUTF();
    }

    @Override
    public void encode(ByteArrayDataOutput stream) {
        stream.writeUTF(this.playerName);

        stream.writeUTF(this.message);

        stream.writeUTF(this.serverName);
    }

    @Override
    public void handle() {
        String format = ChatColor.translateAlternateColorCodes('&', Survival.getInstance().getSettings().getString("chat-settings.format", ""));

        Bukkit.broadcast(Component.text(format.replace("<player-name>", this.playerName).replace("<message>", this.message).replace("<server>", this.serverName)));
    }
}