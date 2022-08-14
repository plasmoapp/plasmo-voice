package su.plo.voice.proto.packets.udp.cllientbound;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.proto.packets.PacketUtil;
import su.plo.voice.proto.packets.udp.bothbound.BaseAudioPacket;

import java.io.IOException;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

@NoArgsConstructor
@ToString
public final class SourceAudioPacket extends BaseAudioPacket<ClientPacketUdpHandler> {

    @Getter
    private UUID sourceId;
    @Getter
    private short distance;

    public SourceAudioPacket(long sequenceNumber, byte[] data, @NotNull UUID sourceId, short distance) {
        super(sequenceNumber, data);
        this.sourceId = sourceId;
        this.distance = distance;
    }

    @Override
    public void read(ByteArrayDataInput in) throws IOException {
        super.read(in);

        this.sourceId = PacketUtil.readUUID(in);
        this.distance = in.readShort();
    }

    @Override
    public void write(ByteArrayDataOutput out) throws IOException {
        super.write(out);

        PacketUtil.writeUUID(out, checkNotNull(sourceId, "sourceId"));
        out.writeShort(distance);
    }

    @Override
    public void handle(ClientPacketUdpHandler handler) {
        handler.handle(this);
    }
}
