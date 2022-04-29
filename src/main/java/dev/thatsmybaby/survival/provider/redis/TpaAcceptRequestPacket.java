package dev.thatsmybaby.survival.provider.redis;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import dev.thatsmybaby.survival.Placeholders;
import dev.thatsmybaby.survival.command.HomeCommand;
import dev.thatsmybaby.survival.object.PlayerSync;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class TpaAcceptRequestPacket extends RedisMessage {

    public String whoAccept;

    public UUID whoAcceptUniqueId;
    public UUID whoReceive;

    public String serverName;
    public String locationString;

    public TpaAcceptRequestPacket() {
        super(2);
    }

    @Override
    public void decode(ByteArrayDataInput stream) {
        this.whoAccept = stream.readUTF();

        this.whoAcceptUniqueId = UUID.fromString(stream.readUTF());

        this.whoReceive = UUID.fromString(stream.readUTF());

        this.serverName = stream.readUTF();

        this.locationString = stream.readUTF();
    }

    @Override
    public void encode(ByteArrayDataOutput stream) {
        stream.writeUTF(this.whoAccept);

        stream.writeUTF(this.whoAcceptUniqueId.toString());

        stream.writeUTF(this.whoReceive.toString());

        stream.writeUTF(this.serverName);

        stream.writeUTF(this.locationString);
    }

    @Override
    public void handle() {
        PlayerSync playerSync = PlayerSync.of(this.whoReceive);

        if (playerSync == null) {
            return;
        }

        playerSync.cancelRunnable(this.whoAcceptUniqueId);

        Player targetPlayer = Bukkit.getPlayer(this.whoReceive);

        if (targetPlayer == null) {
            return;
        }

        targetPlayer.sendMessage(Placeholders.replacePlaceholders("TARGET_TPA_REQUEST_SUCCESSFULLY_ACCEPTED", this.whoAccept));

        HomeCommand.doTeleportQueue(targetPlayer, playerSync, this.locationString, this.serverName);
    }
}