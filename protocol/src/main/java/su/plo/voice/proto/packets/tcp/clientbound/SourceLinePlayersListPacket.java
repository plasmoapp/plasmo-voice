package su.plo.voice.proto.packets.tcp.clientbound;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.proto.data.player.MinecraftGameProfile;
import su.plo.voice.proto.packets.Packet;
import su.plo.voice.proto.packets.PacketUtil;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

@AllArgsConstructor
@NoArgsConstructor
@ToString
public final class SourceLinePlayersListPacket implements Packet<ClientPacketTcpHandler> {

    @Getter
    private UUID lineId;
    @Getter
    private List<MinecraftGameProfile> players;

    public SourceLinePlayersListPacket(@NotNull UUID lineId) {
        this.lineId = lineId;
        this.players = ImmutableList.of();
    }

    @Override
    public void read(ByteArrayDataInput in) throws IOException {
        this.lineId = PacketUtil.readUUID(in);
        this.players = Lists.newArrayList();
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            MinecraftGameProfile player = new MinecraftGameProfile();
            player.deserialize(in);
            players.add(player);
        }
    }

    @Override
    public void write(ByteArrayDataOutput out) throws IOException {
        PacketUtil.writeUUID(out, checkNotNull(lineId));
        out.writeInt(checkNotNull(players).size());
        for (MinecraftGameProfile player : players) {
            player.serialize(out);
        }
    }

    @Override
    public void handle(ClientPacketTcpHandler handler) {
        handler.handle(this);
    }
}
