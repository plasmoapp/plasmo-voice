package su.plo.voice.proto.packets.tcp.clientbound;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import su.plo.voice.proto.data.audio.line.VoiceSourceLine;
import su.plo.voice.proto.packets.Packet;

import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

@AllArgsConstructor
@NoArgsConstructor
@ToString
public final class SourceLineRegisterPacket implements Packet<ClientPacketTcpHandler> {

    @Getter
    private VoiceSourceLine sourceLine;

    @Override
    public void read(ByteArrayDataInput in) throws IOException {
        this.sourceLine = new VoiceSourceLine();
        sourceLine.deserialize(in);
    }

    @Override
    public void write(ByteArrayDataOutput out) throws IOException {
        checkNotNull(sourceLine).serialize(out);
    }

    @Override
    public void handle(ClientPacketTcpHandler handler) {
        handler.handle(this);
    }
}
