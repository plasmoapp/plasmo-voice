package su.plo.voice.common.packets.tcp;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.AllArgsConstructor;
import lombok.Getter;
import su.plo.voice.common.entities.MutedEntity;
import su.plo.voice.common.packets.Packet;

import java.io.IOException;
import java.util.UUID;

@AllArgsConstructor
public class ClientConnectedPacket implements Packet {
    @Getter
    private UUID client;
    @Getter
    private MutedEntity muted;

    public ClientConnectedPacket() {}

    @Override
    public void write(ByteArrayDataOutput buf) throws IOException {
        buf.writeUTF(client.toString());
        buf.writeByte(muted == null ? 0 : 1);
        if(muted != null) {
            buf.writeUTF(muted.getUuid().toString());
            buf.writeLong(muted.getTo());
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
