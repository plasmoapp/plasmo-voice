package su.plo.voice.common.packets.udp;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import su.plo.voice.common.packets.Packet;

import java.io.IOException;
import java.util.UUID;

@AllArgsConstructor
@Data()
public class VoiceServerPacket implements Packet {
    @Getter
    private byte[] data;
    @Getter
    private UUID from;
    @Getter
    private long sequenceNumber;
    @Getter
    private short distance;

    public VoiceServerPacket() {}

    @Override
    public void read(ByteArrayDataInput buf) throws IOException {
        this.from = UUID.fromString(buf.readUTF());
        int length = buf.readInt();
        byte[] data = new byte[length];
        buf.readFully(data);
        this.data = data;
        this.distance = buf.readShort();
        this.sequenceNumber = buf.readLong();
    }

    @Override
    public void write(ByteArrayDataOutput buf) throws IOException {
        buf.writeUTF(from.toString());
        buf.writeInt(data.length);
        buf.write(data);
        buf.writeShort(distance);
        buf.writeLong(sequenceNumber);
    }
}
