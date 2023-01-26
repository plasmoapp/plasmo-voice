package su.plo.voice.proto.packets.tcp.serverbound;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import su.plo.voice.proto.packets.Packet;

import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

@AllArgsConstructor
@NoArgsConstructor
@ToString
public final class LanguageRequestPacket implements Packet<ServerPacketTcpHandler> {

    @Getter
    private String language;

    @Override
    public void read(ByteArrayDataInput in) throws IOException {
        this.language = in.readUTF();
    }

    @Override
    public void write(ByteArrayDataOutput out) throws IOException {
        out.writeUTF(checkNotNull(language));
    }

    @Override
    public void handle(ServerPacketTcpHandler handler) {
        handler.handle(this);
    }
}
