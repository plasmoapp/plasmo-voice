package su.plo.voice.common.packets.tcp;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import su.plo.voice.common.packets.Packet;

import java.io.IOException;
import java.util.UUID;

public class ClientDisconnectedPacket implements Packet {
    private UUID client;

    public ClientDisconnectedPacket() {

    }

    public ClientDisconnectedPacket(UUID client) {
        this.client = client;
    }

    public UUID getClient() {
        return client;
    }

    @Override
    public void write(ByteArrayDataOutput buf) throws IOException {
        buf.writeUTF(client.toString());
    }

    @Override
    public void read(ByteArrayDataInput buf) throws IOException {
        client = UUID.fromString(buf.readUTF());
    }
}
