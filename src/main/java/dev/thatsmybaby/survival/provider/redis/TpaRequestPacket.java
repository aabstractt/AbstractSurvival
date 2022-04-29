package dev.thatsmybaby.survival.provider.redis;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import dev.thatsmybaby.survival.Placeholders;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class TpaRequestPacket extends RedisMessage {

    public String whoSent;

    public UUID whoReceive;

    public TpaRequestPacket() {
        super(1);
    }

    @Override
    public void decode(ByteArrayDataInput stream) {
        this.whoSent = stream.readUTF();

        this.whoReceive = UUID.fromString(stream.readUTF());
    }

    @Override
    public void encode(ByteArrayDataOutput stream) {
        stream.writeUTF(this.whoSent);

        stream.writeUTF(this.whoReceive.toString());
    }

    @Override
    public void handle() {
        Player targetPlayer = Bukkit.getPlayer(this.whoReceive);

        if (targetPlayer != null) {
            targetPlayer.sendMessage(Placeholders.replacePlaceholders("TPA_REQUEST_RECEIVED", this.whoSent));
        }
    }
}