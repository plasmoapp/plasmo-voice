package su.plo.voice.common.packets.tcp;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.AllArgsConstructor;
import lombok.Getter;
import su.plo.voice.common.packets.Packet;

import java.io.IOException;

@AllArgsConstructor
public class ClientConnectPacket implements Packet {
    @Getter
    private String token;
    @Getter
    private String version;

    public ClientConnectPacket() {}

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
