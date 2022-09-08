package su.plo.voice.proto.packets.udp.serverbound;

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
public final class PlayerAudioPacket extends BaseAudioPacket<ServerPacketUdpHandler> {

    @Getter
    private UUID activationId;
    @Getter
    private short distance;
    @Getter
    private boolean stereo;

    public PlayerAudioPacket(long sequenceNumber, byte[] data, @NotNull UUID activationId, short distance, boolean stereo) {
        super(sequenceNumber, data);

        this.activationId = checkNotNull(activationId);
        this.distance = distance;
        this.stereo = stereo;
    }

    @Override
    public void read(ByteArrayDataInput in) throws IOException {
        super.read(in);

        this.activationId = PacketUtil.readUUID(in);
        this.distance = in.readShort();
        this.stereo = in.readBoolean();
    }

    @Override
    public void write(ByteArrayDataOutput out) throws IOException {
        super.write(out);

        PacketUtil.writeUUID(out, checkNotNull(activationId));
        out.writeShort(distance);
        out.writeBoolean(stereo);
    }

    @Override
    public void handle(ServerPacketUdpHandler handler) {
        handler.handle(this);
    }
}
