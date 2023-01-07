package su.plo.voice.common.packets.udp;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import su.plo.voice.common.packets.Packet;

import java.io.IOException;

public class VoiceClientPacket implements Packet {
    private byte[] data;
    private long sequenceNumber;
    private short distance;

    public VoiceClientPacket(byte[] data, long sequenceNumber, short distance) {
        this.data = data;
        this.sequenceNumber = sequenceNumber;
        this.distance = distance;
    }

    public VoiceClientPacket() {}

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public byte[] getData() {
        return data;
    }

    public short getDistance() {
        return distance;
    }

    @Override
    public void read(ByteArrayDataInput buf) throws IOException {
        int length = buf.readInt();
        if (length <= 0 || length > 2048) {
            throw new IOException("Invalid voice data length: " + length);
        }

        byte[] data = new byte[length];
        buf.readFully(data);
        this.data = data;
        this.distance = buf.readShort();
        this.sequenceNumber = buf.readLong();
    }

    @Override
    public void write(ByteArrayDataOutput buf) throws IOException {
        buf.writeInt(data.length);
        buf.write(data);
        buf.writeShort(distance);
        buf.writeLong(sequenceNumber);
    }
}
