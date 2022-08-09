package su.plo.voice.proto.packets.udp.serverbound;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.IOException;

@NoArgsConstructor
@ToString
public final class PlayerAudioPacket extends BaseAudioPacket {

    @Getter
    private short distance;

    public PlayerAudioPacket(long sequenceNumber, byte[] data, short distance) {
        super(sequenceNumber, data);
        this.distance = distance;
    }

    @Override
    public void read(ByteArrayDataInput in) throws IOException {
        super.read(in);

        this.distance = in.readShort();
    }

    @Override
    public void write(ByteArrayDataOutput out) throws IOException {
        super.write(out);

        out.writeShort(distance);
    }

    @Override
    public void handle(ServerPacketUdpHandler handler) {
        handler.handle(this);
    }
}
