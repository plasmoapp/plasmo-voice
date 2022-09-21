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
public final class SourceLinePlayersClearPacket implements Packet<ClientPacketTcpHandler> {

    @Getter
    private UUID lineId;

    @Override
    public void read(ByteArrayDataInput in) throws IOException {
        this.lineId = PacketUtil.readUUID(in);
    }

    @Override
    public void write(ByteArrayDataOutput out) throws IOException {
        PacketUtil.writeUUID(out, checkNotNull(lineId));
    }

    @Override
    public void handle(ClientPacketTcpHandler handler) {
        handler.handle(this);
    }
}
