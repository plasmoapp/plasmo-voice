package su.plo.voice.common.packets.udp;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import su.plo.voice.common.packets.Packet;

import java.io.IOException;
import java.util.UUID;

public class VoiceServerPacket implements Packet {
    private byte[] data;
    private UUID from;
    private long sequenceNumber;
    private short distance;

    public VoiceServerPacket(byte[] data, UUID from, long sequenceNumber, short distance) {
        this.data = data;
        this.from = from;
        this.sequenceNumber = sequenceNumber;
        this.distance = distance;
    }

    public VoiceServerPacket() {}

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public byte[] getData() {
        return data;
    }

    public UUID getFrom() {
        return from;
    }

    public short getDistance() {
        return distance;
    }

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
