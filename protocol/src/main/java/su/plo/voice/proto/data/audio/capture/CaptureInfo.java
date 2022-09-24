package su.plo.voice.proto.data.audio.capture;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import su.plo.voice.proto.data.audio.codec.CodecInfo;
import su.plo.voice.proto.packets.PacketSerializable;

@AllArgsConstructor
@NoArgsConstructor
@ToString
public final class CaptureInfo implements PacketSerializable {

    @Getter
    private int sampleRate;
    @Getter
    private int mtuSize;
    @Getter
    private CodecInfo codec;

    @Override
    public void deserialize(ByteArrayDataInput in) {
        this.sampleRate = in.readInt();
        this.mtuSize = in.readInt();
        if (in.readBoolean()) {
            this.codec = new CodecInfo();
            codec.deserialize(in);
        }
    }

    @Override
    public void serialize(ByteArrayDataOutput out) {
        out.writeInt(sampleRate);
        out.writeInt(mtuSize);
        out.writeBoolean(codec != null);
        if (codec != null) codec.serialize(out);
    }
}
