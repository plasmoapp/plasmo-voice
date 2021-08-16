package su.plo.voice.common.packets.udp;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.AllArgsConstructor;
import lombok.Getter;
import su.plo.voice.common.packets.Packet;

import java.io.IOException;

@AllArgsConstructor
public class AuthPacket implements Packet {
    @Getter
    private String token;

    public AuthPacket() {}

    @Override
    public void read(ByteArrayDataInput buf) throws IOException {
        this.token = buf.readUTF();
    }

    @Override
    public void write(ByteArrayDataOutput buf) throws IOException {
        buf.writeUTF(token);;
    }
}
