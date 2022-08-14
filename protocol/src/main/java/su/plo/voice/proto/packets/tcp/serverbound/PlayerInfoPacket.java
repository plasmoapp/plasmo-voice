package su.plo.voice.proto.packets.tcp.serverbound;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

@AllArgsConstructor
@NoArgsConstructor
@ToString
public final class PlayerInfoPacket extends PlayerStatePacket {

    @Getter
    private String version;

    @Override
    public void read(ByteArrayDataInput in) throws IOException {
        this.version = in.readUTF();
    }

    @Override
    public void write(ByteArrayDataOutput out) throws IOException {
        out.writeUTF(checkNotNull(version));
    }

    @Override
    public void handle(ServerPacketTcpHandler handler) {
        handler.handle(this);
    }
}
