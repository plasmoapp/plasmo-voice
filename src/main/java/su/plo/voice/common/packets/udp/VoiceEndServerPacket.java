package su.plo.voice.common.packets.udp;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import su.plo.voice.common.packets.Packet;

import java.io.IOException;
import java.util.UUID;

public class VoiceEndServerPacket implements Packet {
    private UUID from;

    public VoiceEndServerPacket(UUID from) {
        this.from = from;
    }

    public VoiceEndServerPacket() {}

    public UUID getFrom() {
        return from;
    }

    @Override
    public void read(ByteArrayDataInput buf) throws IOException {
        this.from = UUID.fromString(buf.readUTF());
    }

    @Override
    public void write(ByteArrayDataOutput buf) throws IOException {
        buf.writeUTF(from.toString());
    }
}
