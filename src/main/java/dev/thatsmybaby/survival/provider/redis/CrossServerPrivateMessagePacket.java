package dev.thatsmybaby.survival.provider.redis;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import dev.thatsmybaby.survival.Placeholders;
import dev.thatsmybaby.survival.command.MessageCommand;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class CrossServerPrivateMessagePacket extends RedisMessage {

    public UUID senderUuidParsed;
    public UUID targetUuidParsed;

    public String senderName;

    public String message;

    public CrossServerPrivateMessagePacket() {
        super(4);
    }

    @Override
    public void decode(ByteArrayDataInput stream) {
        this.senderUuidParsed = UUID.fromString(stream.readUTF());

        this.targetUuidParsed = UUID.fromString(stream.readUTF());

        this.senderName = stream.readUTF();

        this.message = stream.readUTF();
    }

    @Override
    public void encode(ByteArrayDataOutput stream) {
        stream.writeUTF(this.senderUuidParsed.toString());

        stream.writeUTF(this.targetUuidParsed.toString());

        stream.writeUTF(this.senderName);

        stream.writeUTF(this.message);
    }

    @Override
    public void handle() {
        Player target = Bukkit.getPlayer(this.targetUuidParsed);

        if (target == null) {
            return;
        }

        MessageCommand.lastMessageReceived.put(target.getUniqueId(), this.senderUuidParsed);

        target.sendMessage(Placeholders.replacePlaceholders("PRIVATE_MESSAGE_RECEIVED", this.senderName, this.message));
    }
}