package su.plo.voice.common.packets.tcp;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.AllArgsConstructor;
import lombok.Getter;
import su.plo.voice.common.packets.Packet;

import java.io.IOException;
import java.util.UUID;

@AllArgsConstructor
public class ClientMutedPacket implements Packet {
    @Getter
    private UUID client;
    @Getter
    private Long to;

    public ClientMutedPacket() {}

    @Override
    public void write(ByteArrayDataOutput buf) throws IOException {
        buf.writeUTF(client.toString());
        buf.writeLong(to);
    }

    @Override
    public void read(ByteArrayDataInput buf) throws IOException {
        client = UUID.fromString(buf.readUTF());
        to = buf.readLong();
    }
}
