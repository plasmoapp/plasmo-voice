package su.plo.voice.common.packets.udp;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import su.plo.voice.common.packets.Packet;

import java.io.IOException;

public class VoiceEndClientPacket implements Packet {
    private short distance;

    public VoiceEndClientPacket() {}

    public VoiceEndClientPacket(short distance) {
        this.distance = distance;
    }

    public short getDistance() {
        return distance;
    }

    @Override
    public void read(ByteArrayDataInput buf) throws IOException {
        this.distance = buf.readShort();
    }

    @Override
    public void write(ByteArrayDataOutput buf) throws IOException {
        buf.writeShort(distance);
    }
}
