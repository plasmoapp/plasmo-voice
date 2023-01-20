package su.plo.voice.proto.data.encryption;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import su.plo.voice.proto.packets.PacketSerializable;
import su.plo.voice.proto.packets.PacketUtil;

import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

@AllArgsConstructor
@NoArgsConstructor
@ToString
public final class EncryptionInfo implements PacketSerializable {

    @Getter
    private String algorithm;

    @Getter
    private byte[] data;

    @Override
    public void deserialize(ByteArrayDataInput in) throws IOException {
        this.algorithm = in.readUTF();

        int length = PacketUtil.readSafeInt(in, 1, Integer.MAX_VALUE);
        byte[] data = new byte[length];
        in.readFully(data);
        this.data = data;
    }

    @Override
    public void serialize(ByteArrayDataOutput out) {
        checkNotNull(algorithm, "algorithm cannot be null");
        checkNotNull(data, "data cannot be null");

        out.writeUTF(algorithm);

        out.writeInt(data.length);
        out.write(data);
    }
}
