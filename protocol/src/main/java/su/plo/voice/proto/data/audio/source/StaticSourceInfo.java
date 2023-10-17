package su.plo.voice.proto.data.audio.source;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.slib.api.position.Pos3d;
import su.plo.voice.proto.data.audio.codec.CodecInfo;
import su.plo.voice.proto.serializer.Pos3dSerializer;

import java.io.IOException;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

@NoArgsConstructor
@ToString(callSuper = true)
public final class StaticSourceInfo extends SourceInfo {

    @Getter
    private Pos3d position;
    @Getter
    private Pos3d lookAngle;

    public StaticSourceInfo(@NotNull String addonId,
                            @NotNull UUID sourceId,
                            @NotNull UUID lineId,
                            @Nullable String sourceName,
                            byte state,
                            @Nullable CodecInfo decoderInfo,
                            boolean stereo,
                            boolean iconVisible,
                            int angle,
                            Pos3d position,
                            Pos3d lookAngle) {
        super(addonId, sourceId, lineId, sourceName, state, decoderInfo, stereo, iconVisible, angle);

        this.position = position;
        this.lookAngle = lookAngle;
    }

    @Override
    public void deserialize(ByteArrayDataInput in) throws IOException {
        super.deserialize(in);

        this.position = Pos3dSerializer.INSTANCE.deserialize(in);
        this.lookAngle = Pos3dSerializer.INSTANCE.deserialize(in);
    }

    @Override
    public void serialize(ByteArrayDataOutput out) throws IOException {
        super.serialize(out);

        Pos3dSerializer.INSTANCE.serialize(checkNotNull(position), out);
        Pos3dSerializer.INSTANCE.serialize(checkNotNull(lookAngle), out);
    }

    @Override
    public Type getType() {
        return Type.STATIC;
    }
}
