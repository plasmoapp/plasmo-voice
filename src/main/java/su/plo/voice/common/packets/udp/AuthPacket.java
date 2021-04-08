package su.plo.voice.common.packets.udp;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import su.plo.voice.common.packets.Packet;

import java.io.IOException;

public class AuthPacket implements Packet {
    private String token;

    public AuthPacket(String token) {
        this.token = token;
    }

    public AuthPacket() {}

    public String getToken() {
        return token;
    }

    @Override
    public void read(ByteArrayDataInput buf) throws IOException {
        this.token = buf.readUTF();
    }

    @Override
    public void write(ByteArrayDataOutput buf) throws IOException {
        buf.writeUTF(token);;
    }
}
