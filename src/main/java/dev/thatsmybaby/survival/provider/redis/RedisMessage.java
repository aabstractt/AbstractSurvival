package dev.thatsmybaby.survival.provider.redis;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.Getter;

public abstract class RedisMessage {

    @Getter protected int id;

    public RedisMessage(int id) {
        this.id = id;
    }

    public abstract void decode(ByteArrayDataInput stream);

    public abstract void encode(ByteArrayDataOutput stream);

    public abstract void handle();

    public boolean isAsync() {
        return this.getClass().isAnnotationPresent(AsyncPacket.class);
    }
}