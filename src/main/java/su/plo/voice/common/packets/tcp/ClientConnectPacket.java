package su.plo.voice.common.packets.tcp;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import su.plo.voice.common.packets.Packet;

import java.io.IOException;

public class ClientConnectPacket implements Packet {
    private String token;
    private String version;

    public ClientConnectPacket() {}

    public ClientConnectPacket(String token, String version) {
        this.token = token;
        this.version = version;
    }

    public String getToken() {
        return token;
    }

    public String getVersion() {
        return version;
    }


    @Override
    public void read(ByteArrayDataInput buf) throws IOException {
        this.token = buf.readUTF();
        this.version = buf.readUTF();
    }

    @Override
    public void write(ByteArrayDataOutput buf) throws IOException {
        buf.writeUTF(token);
        buf.writeUTF(version);
    }
}
