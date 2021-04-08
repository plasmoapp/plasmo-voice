package su.plo.voice.common.packets.tcp;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import su.plo.voice.common.packets.Packet;

import java.io.IOException;

public class ServerConnectPacket implements Packet {
    private String token;
    private String ip;
    private int port;
    private boolean priority;

    public ServerConnectPacket() {}

    public ServerConnectPacket(String token, String ip, int port, boolean priority) {
        this.token = token;
        this.ip = ip;
        this.port = port;
        this.priority = priority;
    }

    public int getPort() {
        return port;
    }

    public String getIp() {
        return ip;
    }

    public String getToken() {
        return token;
    }

    public boolean hasPriority() {
        return priority;
    }

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
