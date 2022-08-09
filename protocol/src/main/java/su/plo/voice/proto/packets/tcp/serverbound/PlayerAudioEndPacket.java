package su.plo.voice.proto.packets.tcp.serverbound;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.Getter;
import su.plo.voice.proto.packets.Packet;

import java.io.IOException;

public final class PlayerAudioEndPacket implements Packet<ServerPacketTcpHandler> {

    @Getter
    private long sequenceNumber;

    @Override
    public void read(ByteArrayDataInput in) throws IOException {
        this.sequenceNumber = in.readLong();
    }

    @Override
    public void write(ByteArrayDataOutput out) throws IOException {
        out.writeLong(sequenceNumber);
    }

    @Override
    public void handle(ServerPacketTcpHandler handler) {
        handler.handle(this);
    }
}
