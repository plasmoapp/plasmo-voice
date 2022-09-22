package su.plo.voice.proto.packets.tcp.clientbound;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import su.plo.voice.proto.data.audio.capture.VoiceActivation;
import su.plo.voice.proto.packets.Packet;

import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

@AllArgsConstructor
@NoArgsConstructor
@ToString
public final class ActivationRegisterPacket implements Packet<ClientPacketTcpHandler> {

    @Getter
    private VoiceActivation activation;

    @Override
    public void read(ByteArrayDataInput in) throws IOException {
        this.activation = new VoiceActivation();
        activation.deserialize(in);
    }

    @Override
    public void write(ByteArrayDataOutput out) throws IOException {
        checkNotNull(activation).serialize(out);
    }

    @Override
    public void handle(ClientPacketTcpHandler handler) {
        handler.handle(this);
    }
}
