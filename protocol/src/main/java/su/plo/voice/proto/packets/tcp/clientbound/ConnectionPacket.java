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
public final class ConnectionPacket implements Packet<ClientPacketTcpHandler> {

    @Getter
    private UUID secret;
    @Getter
    private String ip;
    @Getter
    private int port;

    @Override
    public void read(ByteArrayDataInput in) throws IOException {
        this.secret = PacketUtil.readUUID(in);
        this.ip = in.readUTF();
        this.port = in.readInt();
    }

    @Override
    public void write(ByteArrayDataOutput out) throws IOException {
        checkNotNull(secret, "secret cannot be null");
        checkNotNull(ip, "ip cannot be null");

        PacketUtil.writeUUID(out, secret);
        out.writeUTF(ip);
        out.writeInt(port);
    }

    @Override
    public void handle(ClientPacketTcpHandler handler) {
        handler.handle(this);
    }
}
