package su.plo.voice.common.packets.udp;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.AllArgsConstructor;
import lombok.Getter;
import su.plo.voice.common.packets.Packet;

import java.io.IOException;
import java.util.UUID;

@AllArgsConstructor
public class VoiceEndServerPacket implements Packet {
    @Getter
    private UUID from;

    public VoiceEndServerPacket() {}

    @Override
    public void read(ByteArrayDataInput buf) throws IOException {
        this.from = UUID.fromString(buf.readUTF());
    }

    @Override
    public void write(ByteArrayDataOutput buf) throws IOException {
        buf.writeUTF(from.toString());
    }
}
