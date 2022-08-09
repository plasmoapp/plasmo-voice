package su.plo.voice.proto.packets.udp.serverbound;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.Getter;

import java.io.IOException;

public final class PlayerAudioPacket extends BaseAudioPacket {

    @Getter
    private short distance;

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
