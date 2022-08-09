package su.plo.voice.proto.packets.tcp.clientbound;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.proto.packets.PacketUtil;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@ToString
public final class ConfigPacket extends ConfigPlayerInfoPacket {

    @Getter
    private UUID serverId;

    @Getter
    private int sampleRate;

    @Getter
    private List<Integer> distances;

    @Getter
    private int maxPriorityDistance;

    public ConfigPacket(@NotNull UUID serverId, int sampleRate, List<Integer> distances, int maxPriorityDistance) {
        this.serverId = serverId;
        this.sampleRate = sampleRate;
        this.distances = distances;
        this.maxPriorityDistance = maxPriorityDistance;
    }

    @Override
    public void read(ByteArrayDataInput in) throws IOException {
        this.serverId = PacketUtil.readUUID(in);
        this.sampleRate = in.readInt();
        this.distances = PacketUtil.readIntList(in);
        this.maxPriorityDistance = in.readInt();
    }

    @Override
    public void write(ByteArrayDataOutput out) throws IOException {
        PacketUtil.writeUUID(out, serverId);
        out.writeInt(sampleRate);
        PacketUtil.writeIntList(out, distances);
        out.writeInt(maxPriorityDistance);
    }

    @Override
    public void handle(ClientPacketTcpHandler handler) {
        handler.handle(this);
    }
}
