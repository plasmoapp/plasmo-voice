package su.plo.voice.proto.data;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import su.plo.voice.proto.packets.PacketSerializable;

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
    public void deserialize(ByteArrayDataInput in) {
        this.algorithm = in.readUTF();

        byte[] data = new byte[in.readInt()];
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
