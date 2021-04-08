package su.plo.voice.common.packets.tcp;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import su.plo.voice.common.packets.Packet;

import java.io.IOException;
import java.util.UUID;

public class ClientMutedPacket implements Packet {
    private UUID client;
    private Long to;

    public ClientMutedPacket() {}

    public ClientMutedPacket(UUID client, Long to) {
        this.client = client;
        this.to = to;
    }

    public UUID getClient() {
        return client;
    }

    public Long getTo() {
        return to;
    }

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
