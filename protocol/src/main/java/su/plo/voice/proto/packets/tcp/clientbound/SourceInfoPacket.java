package su.plo.voice.proto.packets.tcp.clientbound;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import su.plo.voice.proto.data.source.SourceInfo;
import su.plo.voice.proto.packets.Packet;

import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

@AllArgsConstructor
@NoArgsConstructor
@ToString
public class SourceInfoPacket implements Packet<ClientPacketTcpHandler> {

    @Getter
    private SourceInfo sourceInfo;

    @Override
    public void read(ByteArrayDataInput in) throws IOException {
        this.sourceInfo = SourceInfo.of(in);
    }

    @Override
    public void write(ByteArrayDataOutput out) throws IOException {
        checkNotNull(sourceInfo).serialize(out);
    }

    @Override
    public void handle(ClientPacketTcpHandler handler) {
        handler.handle(this);
    }
}
