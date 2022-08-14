package su.plo.voice.proto.packets.tcp.serverbound;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import su.plo.voice.proto.packets.Packet;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PlayerStatePacket implements Packet<ServerPacketTcpHandler> {

    @Getter
    private boolean voiceDisabled;
    @Getter
    private boolean microphoneMuted;

    @Override
    public void read(ByteArrayDataInput in) throws IOException {
        this.voiceDisabled = in.readBoolean();
        this.microphoneMuted = in.readBoolean();
    }

    @Override
    public void write(ByteArrayDataOutput out) throws IOException {
        out.writeBoolean(voiceDisabled);
        out.writeBoolean(microphoneMuted);
    }

    @Override
    public void handle(ServerPacketTcpHandler handler) {
        handler.handle(this);
    }
}
