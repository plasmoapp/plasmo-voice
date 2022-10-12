package su.plo.voice.proto.packets.tcp.clientbound;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import su.plo.voice.proto.data.player.VoicePlayerInfo;
import su.plo.voice.proto.packets.Packet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

@AllArgsConstructor
@NoArgsConstructor
@ToString
public final class PlayerListPacket implements Packet<ClientPacketTcpHandler> {

    @Getter
    private List<VoicePlayerInfo> players;

    @Override
    public void read(ByteArrayDataInput in) throws IOException {
        int size = in.readInt();
        this.players = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            VoicePlayerInfo player = new VoicePlayerInfo();
            player.deserialize(in);
            players.add(player);
        }
    }

    @Override
    public void write(ByteArrayDataOutput out) throws IOException {
        checkNotNull(players, "players");
        out.writeInt(players.size());
        for (VoicePlayerInfo player : players) player.serialize(out);
    }

    @Override
    public void handle(ClientPacketTcpHandler handler) {
        handler.handle(this);
    }
}
