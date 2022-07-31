package su.plo.voice.proto.packets.udp;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.Getter;
import su.plo.voice.proto.packets.Packet;

import java.io.IOException;

public abstract class PingPacket<T> implements Packet<T> {

    @Getter
    private long time;

    @Override
    public void read(ByteArrayDataInput in) throws IOException {
        this.time = in.readLong();
    }

    @Override
    public void write(ByteArrayDataOutput out) throws IOException {
        out.writeLong(System.currentTimeMillis());
    }
}
