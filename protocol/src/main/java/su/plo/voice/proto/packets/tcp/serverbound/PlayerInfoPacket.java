package su.plo.voice.proto.packets.tcp.serverbound;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.proto.packets.PacketUtil;

import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

@NoArgsConstructor
@ToString(callSuper = true)
public final class PlayerInfoPacket extends PlayerStatePacket {

    @Getter
    private String version;
    @Getter
    private byte[] publicKey;

    public PlayerInfoPacket(@NotNull String version,
                            byte[] publicKey,
                            boolean voiceDisabled,
                            boolean microphoneDisabled) {
        super(voiceDisabled, microphoneDisabled);

        this.version = version;
        this.publicKey = checkNotNull(publicKey, "publicKey");
    }

    @Override
    public void read(ByteArrayDataInput in) throws IOException {
        super.read(in);

        this.version = in.readUTF();

        int length = PacketUtil.readSafeInt(in, 1, 2048);
        this.publicKey = new byte[length];
        in.readFully(publicKey);
    }

    @Override
    public void write(ByteArrayDataOutput out) throws IOException {
        super.write(out);

        out.writeUTF(checkNotNull(version));

        checkNotNull(publicKey);
        out.writeInt(publicKey.length);
        out.write(publicKey);
    }

    @Override
    public void handle(ServerPacketTcpHandler handler) {
        handler.handle(this);
    }
}
