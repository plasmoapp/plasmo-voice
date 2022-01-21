package su.plo.voice.common.packets.tcp;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import su.plo.voice.common.entities.MutedEntity;
import su.plo.voice.common.packets.Packet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ClientsListPacket implements Packet {
    private final List<UUID> clients;
    private final List<MutedEntity> muted;

    public ClientsListPacket() {
        clients = new ArrayList<>();
        muted = new ArrayList<>();
    }

    public ClientsListPacket(List<UUID> clients, List<MutedEntity> muted) {
        this.clients = clients;
        this.muted = muted;
    }

    public List<UUID> getClients() {
        return clients;
    }

    public List<MutedEntity> getMuted() {
        return muted;
    }

    @Override
    public void write(ByteArrayDataOutput buf) throws IOException {
        buf.writeInt(clients.size());
        for(UUID uuid : clients) {
            buf.writeUTF(uuid.toString());
        }

        buf.writeInt(muted.size());
        for(MutedEntity m : muted) {
            buf.writeUTF(m.uuid.toString());
            buf.writeLong(m.to);
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
