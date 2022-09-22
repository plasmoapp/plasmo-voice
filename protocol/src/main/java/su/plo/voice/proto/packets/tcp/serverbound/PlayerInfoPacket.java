package su.plo.voice.proto.packets.tcp.serverbound;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

@NoArgsConstructor
@ToString(callSuper = true)
public final class PlayerInfoPacket extends PlayerStatePacket {

    @Getter
    private String version;

    public PlayerInfoPacket(@NotNull String version,
                            boolean voiceDisabled,
                            boolean microphoneDisabled) {
        super(voiceDisabled, microphoneDisabled);

        this.version = version;
    }

    @Override
    public void read(ByteArrayDataInput in) throws IOException {
        super.read(in);
        this.version = in.readUTF();
    }

    @Override
    public void write(ByteArrayDataOutput out) throws IOException {
        super.write(out);
        out.writeUTF(checkNotNull(version));
    }

    @Override
    public void handle(ServerPacketTcpHandler handler) {
        handler.handle(this);
    }
}
