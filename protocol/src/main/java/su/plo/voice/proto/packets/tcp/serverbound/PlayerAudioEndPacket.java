package su.plo.voice.proto.packets.tcp.serverbound;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import su.plo.voice.proto.packets.Packet;

import java.io.IOException;

@NoArgsConstructor
@ToString
public final class PlayerAudioEndPacket implements Packet<ServerPacketTcpHandler> {

    @Getter
    private long sequenceNumber;

    @Getter
    private short distance;

    public PlayerAudioEndPacket(long sequenceNumber, short distance) {
        this.sequenceNumber = sequenceNumber;
        this.distance = distance;
    }

    @Override
    public void read(ByteArrayDataInput in) throws IOException {
        this.sequenceNumber = in.readLong();
        this.distance = in.readShort();
    }

    @Override
    public void write(ByteArrayDataOutput out) throws IOException {
        out.writeLong(sequenceNumber);
        out.writeShort(distance);
    }

    @Override
    public void handle(ServerPacketTcpHandler handler) {
        handler.handle(this);
    }
}
