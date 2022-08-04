package su.plo.voice.proto.packets.udp.bothbound;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.Getter;
import lombok.ToString;
import su.plo.voice.proto.packets.Packet;
import su.plo.voice.proto.packets.udp.PacketUdpHandler;

import java.io.IOException;

@ToString
public class PingPacket implements Packet<PacketUdpHandler> {

    @Getter
    private long time = System.currentTimeMillis();

    @Override
    public void read(ByteArrayDataInput in) throws IOException {
        this.time = in.readLong();
    }

    @Override
    public void write(ByteArrayDataOutput out) throws IOException {
        out.writeLong(time);
    }

    @Override
    public void handle(PacketUdpHandler handler) {
        handler.handle(this);
    }
}
