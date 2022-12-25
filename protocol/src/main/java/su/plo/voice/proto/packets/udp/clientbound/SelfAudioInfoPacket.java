package su.plo.voice.proto.packets.udp.clientbound;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import su.plo.voice.proto.packets.Packet;
import su.plo.voice.proto.packets.PacketUtil;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

@AllArgsConstructor
@NoArgsConstructor
@ToString
public final class SelfAudioInfoPacket implements Packet<ClientPacketUdpHandler> {

    @Getter
    private UUID sourceId;
    @Getter
    private long sequenceNumber;
    private byte[] data;
    @Getter
    private short distance;

    public Optional<byte[]> getData() {
        return Optional.ofNullable(data);
    }

    @Override
    public void read(ByteArrayDataInput in) throws IOException {
        this.sourceId = PacketUtil.readUUID(in);
        this.sequenceNumber = in.readLong();
        if (in.readBoolean()) {
            byte[] data = new byte[in.readInt()];
            in.readFully(data);
            this.data = data;
        }
        this.distance = in.readShort();
    }

    @Override
    public void write(ByteArrayDataOutput out) throws IOException {
        PacketUtil.writeUUID(out, checkNotNull(sourceId, "sourceId"));
        out.writeLong(sequenceNumber);
        out.writeBoolean(data != null);
        if (data != null) {
            out.writeInt(data.length);
            out.write(data);
        }
        out.writeShort(distance);
    }

    @Override
    public void handle(ClientPacketUdpHandler handler) {
        handler.handle(this);
    }
}
