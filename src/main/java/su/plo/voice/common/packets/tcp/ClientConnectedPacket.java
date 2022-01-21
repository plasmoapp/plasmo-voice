package su.plo.voice.common.packets.tcp;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import su.plo.voice.common.entities.MutedEntity;
import su.plo.voice.common.packets.Packet;

import java.io.IOException;
import java.util.UUID;

public class ClientConnectedPacket implements Packet {
    private UUID client;
    private MutedEntity muted;

    public ClientConnectedPacket() {}

    public ClientConnectedPacket(UUID client, MutedEntity muted) {
        this.client = client;
        this.muted = muted;
    }

    public UUID getClient() {
        return client;
    }

    public MutedEntity getMuted() {
        return muted;
    }

    @Override
    public void write(ByteArrayDataOutput buf) throws IOException {
        buf.writeUTF(client.toString());
        buf.writeByte(muted == null ? 0 : 1);
        if(muted != null) {
            buf.writeUTF(muted.uuid.toString());
            buf.writeLong(muted.to);
        }
    }

    @Override
    public void read(ByteArrayDataInput buf) throws IOException {
        client = UUID.fromString(buf.readUTF());
        if(buf.readByte() == 1) {
            muted = new MutedEntity(UUID.fromString(buf.readUTF()), buf.readLong());
        }
    }
}
