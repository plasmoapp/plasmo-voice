package su.plo.voice.proto.packets.tcp.clientbound;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import su.plo.voice.proto.data.audio.source.SelfSourceInfo;
import su.plo.voice.proto.packets.Packet;

import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

@AllArgsConstructor
@NoArgsConstructor
@ToString // todo: rename?
public final class SelfSourceInfoPacket implements Packet<ClientPacketTcpHandler> {

    @Getter
    private SelfSourceInfo sourceInfo;

    @Override
    public void read(ByteArrayDataInput in) throws IOException {
        this.sourceInfo = new SelfSourceInfo();
        sourceInfo.deserialize(in);
    }

    @Override
    public void write(ByteArrayDataOutput out) throws IOException {
        checkNotNull(this.sourceInfo, "sourceInfo").serialize(out);
    }

    @Override
    public void handle(ClientPacketTcpHandler handler) {
        handler.handle(this);
    }
}
