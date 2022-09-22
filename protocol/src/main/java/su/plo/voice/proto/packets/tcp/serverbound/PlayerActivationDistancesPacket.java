package su.plo.voice.proto.packets.tcp.serverbound;

import com.google.common.collect.Maps;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import su.plo.voice.proto.packets.Packet;
import su.plo.voice.proto.packets.PacketUtil;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

@AllArgsConstructor
@NoArgsConstructor
@ToString
public final class PlayerActivationDistancesPacket implements Packet<ServerPacketTcpHandler> {

    @Getter
    private Map<UUID, Integer> distanceByActivationId;

    @Override
    public void read(ByteArrayDataInput in) throws IOException {
        this.distanceByActivationId = Maps.newHashMap();
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            distanceByActivationId.put(PacketUtil.readUUID(in), in.readInt());
        }
    }

    @Override
    public void write(ByteArrayDataOutput out) throws IOException {
        out.writeInt(checkNotNull(distanceByActivationId).size());
        distanceByActivationId.forEach((activationId, distance) -> {
            PacketUtil.writeUUID(out, activationId);
            out.writeInt(distance);
        });
    }

    @Override
    public void handle(ServerPacketTcpHandler handler) {

    }
}
