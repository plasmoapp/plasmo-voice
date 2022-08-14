package su.plo.voice.proto.packets.tcp.clientbound;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import su.plo.voice.proto.data.VoicePlayerInfo;
import su.plo.voice.proto.packets.Packet;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PlayerInfoRequestPacket implements Packet<ClientPacketTcpHandler> {

    @Getter
    private VoicePlayerInfo playerInfo;

    @Override
    public void read(ByteArrayDataInput in) throws IOException {
    }

    @Override
    public void write(ByteArrayDataOutput out) throws IOException {
    }

    @Override
    public void handle(ClientPacketTcpHandler handler) {
        handler.handle(this);
    }
}
