package dev.thatsmybaby.survival.provider.redis;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import dev.thatsmybaby.survival.factory.CrossServerTeleportFactory;

@AsyncPacket
public final class UpdateWarpCrossServerPacket extends RedisMessage {

    public UpdateWarpCrossServerPacket() {
        super(5);
    }

    @Override
    public void decode(ByteArrayDataInput stream) {
        // Nothing is decoded
    }

    @Override
    public void encode(ByteArrayDataOutput stream) {
        // Nothing is encoded
    }

    @Override
    public void handle() {
        CrossServerTeleportFactory.getInstance().loadWarpCrossServerLocations();
    }
}