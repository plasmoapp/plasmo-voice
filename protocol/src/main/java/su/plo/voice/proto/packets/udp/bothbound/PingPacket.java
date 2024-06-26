package su.plo.voice.proto.packets.udp.bothbound;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.proto.packets.Packet;
import su.plo.voice.proto.packets.udp.PacketUdpHandler;

import java.io.IOException;

@ToString
@NoArgsConstructor
public class PingPacket implements Packet<PacketUdpHandler> {

    @Getter
    private long time = System.currentTimeMillis();

    @Getter
    private String serverIp;
    @Getter
    private int serverPort;

    public PingPacket(@NotNull String serverIp, int serverPort) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
    }

    @Override
    public void read(ByteArrayDataInput in) throws IOException {
        this.time = in.readLong();

        try {
            this.serverIp = in.readUTF();
            this.serverPort = in.readShort();
        } catch (Exception ignored) {
            // ignore exceptions here, because it's optional
        }
    }

    @Override
    public void write(ByteArrayDataOutput out) throws IOException {
        out.writeLong(time);

        if (serverIp != null && serverPort > 0) {
            out.writeUTF(serverIp);
            out.writeShort(serverPort);
        }
    }

    @Override
    public void handle(PacketUdpHandler handler) {
        handler.handle(this);
    }
}
