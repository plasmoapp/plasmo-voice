package su.plo.voice.proto.packets.tcp.clientbound;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import su.plo.voice.proto.packets.Packet;
import su.plo.voice.proto.packets.PacketUtil;

import java.io.IOException;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

@AllArgsConstructor
@NoArgsConstructor
@ToString
public final class SourceAudioEndPacket implements Packet<ClientPacketTcpHandler> {

    @Getter
    private UUID sourceId;

    @Getter
    private long sequenceNumber;

    @Override
    public void read(ByteArrayDataInput in) throws IOException {
        this.sourceId = PacketUtil.readUUID(in);
        this.sequenceNumber = in.readLong();
    }

    @Override
    public void write(ByteArrayDataOutput out) throws IOException {
        PacketUtil.writeUUID(out, checkNotNull(sourceId));
        out.writeLong(sequenceNumber);
    }

    @Override
    public void handle(ClientPacketTcpHandler handler) {
        handler.handle(this);
    }
}
