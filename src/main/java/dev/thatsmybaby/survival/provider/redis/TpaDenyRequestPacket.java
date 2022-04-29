package dev.thatsmybaby.survival.provider.redis;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import dev.thatsmybaby.survival.Placeholders;
import dev.thatsmybaby.survival.object.PlayerSync;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class TpaDenyRequestPacket extends RedisMessage {

    public String whoDeny;

    public UUID whoDenyUniqueId;

    public UUID whoReceive;

    public TpaDenyRequestPacket() {
        super(3);
    }

    @Override
    public void decode(ByteArrayDataInput stream) {
        this.whoDeny = stream.readUTF();

        this.whoDenyUniqueId = UUID.fromString(stream.readUTF());

        this.whoReceive = UUID.fromString(stream.readUTF());
    }

    @Override
    public void encode(ByteArrayDataOutput stream) {
        stream.writeUTF(this.whoDeny);

        stream.writeUTF(this.whoDenyUniqueId.toString());

        stream.writeUTF(this.whoReceive.toString());
    }

    @Override
    public void handle() {
        PlayerSync playerSync = PlayerSync.of(this.whoReceive);

        if (playerSync == null) {
            return;
        }

        playerSync.cancelRunnable(this.whoDenyUniqueId);

        Player targetPlayer = Bukkit.getPlayer(this.whoReceive);

        if (targetPlayer == null) {
            return;
        }

        targetPlayer.sendMessage(Placeholders.replacePlaceholders("TARGET_TPA_REQUEST_DENIED", this.whoDeny));
    }
}