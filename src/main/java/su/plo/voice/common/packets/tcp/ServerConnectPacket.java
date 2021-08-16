package su.plo.voice.common.packets.tcp;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.AllArgsConstructor;
import lombok.Getter;
import su.plo.voice.common.packets.Packet;

import java.io.IOException;

@AllArgsConstructor
public class ServerConnectPacket implements Packet {
    @Getter
    private String token;
    @Getter
    private String ip;
    @Getter
    private int port;
    @Getter
    private boolean priority;

    public ServerConnectPacket() {}

    @Override
    public void read(ByteArrayDataInput buf) throws IOException {
        this.token = buf.readUTF();
        this.ip = buf.readUTF();
        this.port = buf.readInt();
        this.priority = buf.readBoolean();
    }

    @Override
    public void write(ByteArrayDataOutput buf) throws IOException {
        buf.writeUTF(token);
        buf.writeUTF(ip);
        buf.writeInt(port);
        buf.writeBoolean(priority);
    }
}
