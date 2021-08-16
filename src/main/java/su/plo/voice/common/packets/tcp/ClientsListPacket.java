package su.plo.voice.common.packets.tcp;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.AllArgsConstructor;
import lombok.Getter;
import su.plo.voice.common.entities.MutedEntity;
import su.plo.voice.common.packets.Packet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class ClientsListPacket implements Packet {
    @Getter
    private final List<UUID> clients;
    @Getter
    private final List<MutedEntity> muted;

    public ClientsListPacket() {
        clients = new ArrayList<>();
        muted = new ArrayList<>();
    }

    @Override
    public void write(ByteArrayDataOutput buf) throws IOException {
        buf.writeInt(clients.size());
        for(UUID uuid : clients) {
            buf.writeUTF(uuid.toString());
        }

        buf.writeInt(muted.size());
        for(MutedEntity m : muted) {
            buf.writeUTF(m.getUuid().toString());
            buf.writeLong(m.getTo());
        }
    }

    @Override
    public void read(ByteArrayDataInput buf) throws IOException {
        clients.clear();
        int length = buf.readInt();
        for(int i = 0; i < length; i++) {
            clients.add(UUID.fromString(buf.readUTF()));
        }

        muted.clear();
        length = buf.readInt();
        for(int i = 0; i < length; i++) {
            muted.add(new MutedEntity(UUID.fromString(buf.readUTF()), buf.readLong()));
        }
    }
}
