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

@AllArgsConstructor
@NoArgsConstructor
@ToString
public final class PlayerInfoUpdatePacket implements Packet<ClientPacketTcpHandler> {

    @Getter
    private VoicePlayerInfo playerInfo;

    @Override
    public void read(ByteArrayDataInput in) throws IOException {
        this.playerInfo = new VoicePlayerInfo();
        playerInfo.deserialize(in);
    }

    @Override
    public void write(ByteArrayDataOutput out) throws IOException {
        playerInfo.serialize(out);
    }

    @Override
    public void handle(ClientPacketTcpHandler handler) {
        handler.handle(this);
    }
}
