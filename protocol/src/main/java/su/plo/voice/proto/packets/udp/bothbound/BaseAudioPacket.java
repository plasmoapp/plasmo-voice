package su.plo.voice.proto.packets.udp.bothbound;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import su.plo.voice.proto.packets.Packet;
import su.plo.voice.proto.packets.PacketHandler;
import su.plo.voice.proto.packets.PacketUtil;

import java.io.IOException;

@NoArgsConstructor
@ToString
public abstract class BaseAudioPacket<T extends PacketHandler> implements Packet<T> {

    @Getter
    protected long sequenceNumber;
    @Getter
    protected byte[] data;

    public BaseAudioPacket(long sequenceNumber, byte[] data) {
        this.sequenceNumber = sequenceNumber;
        this.data = data;
    }

    @Override
    public void read(ByteArrayDataInput in) throws IOException {
        this.sequenceNumber = in.readLong();

        int length = PacketUtil.readSafeInt(in, 1, 2048);
        byte[] data = new byte[length];
        in.readFully(data);
        this.data = data;
    }

    @Override
    public void write(ByteArrayDataOutput out) throws IOException {
        out.writeLong(sequenceNumber);

        out.writeInt(data.length);
        out.write(data);
    }
}
