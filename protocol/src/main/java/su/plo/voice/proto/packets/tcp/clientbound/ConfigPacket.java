package su.plo.voice.proto.packets.tcp.clientbound;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import su.plo.voice.proto.packets.PacketUtil;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@ToString
public final class ConfigPacket extends ConfigPlayerInfoPacket {

    @Getter
    private UUID serverId;

    @Getter
    private int sampleRate;

    @Getter
    private String codec;

    @Getter
    private List<Integer> distances;

    @Getter
    private int defaultDistance;

    @Getter
    private int maxPriorityDistance;

    @Override
    public void read(ByteArrayDataInput in) throws IOException {
        this.serverId = PacketUtil.readUUID(in);
        this.sampleRate = in.readInt();
        this.codec = PacketUtil.readNullableString(in);
        this.distances = PacketUtil.readIntList(in);
        this.defaultDistance = in.readInt();
        this.maxPriorityDistance = in.readInt();
    }

    @Override
    public void write(ByteArrayDataOutput out) throws IOException {
        PacketUtil.writeUUID(out, serverId);
        out.writeInt(sampleRate);
        PacketUtil.writeNullableString(out, codec);
        PacketUtil.writeIntList(out, distances);
        out.writeInt(defaultDistance);
        out.writeInt(maxPriorityDistance);
    }

    @Override
    public void handle(ClientPacketTcpHandler handler) {
        handler.handle(this);
    }
}
