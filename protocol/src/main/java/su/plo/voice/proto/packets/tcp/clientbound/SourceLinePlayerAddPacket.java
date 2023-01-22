package su.plo.voice.proto.packets.tcp.clientbound;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import su.plo.voice.proto.data.player.MinecraftGameProfile;
import su.plo.voice.proto.packets.Packet;
import su.plo.voice.proto.packets.PacketUtil;

import java.io.IOException;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

@AllArgsConstructor
@NoArgsConstructor
@ToString
public final class SourceLinePlayerAddPacket implements Packet<ClientPacketTcpHandler> {

    @Getter
    private UUID lineId;
    @Getter
    private MinecraftGameProfile player;

    @Override
    public void read(ByteArrayDataInput in) throws IOException {
        this.lineId = PacketUtil.readUUID(in);
        this.player = new MinecraftGameProfile();
        player.deserialize(in);
    }

    @Override
    public void write(ByteArrayDataOutput out) throws IOException {
        PacketUtil.writeUUID(out, checkNotNull(lineId));
        checkNotNull(player).serialize(out);
    }

    @Override
    public void handle(ClientPacketTcpHandler handler) {
        handler.handle(this);
    }
}
