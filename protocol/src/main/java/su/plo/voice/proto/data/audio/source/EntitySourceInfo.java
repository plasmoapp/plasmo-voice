package su.plo.voice.proto.data.audio.source;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.proto.data.audio.codec.CodecInfo;

import java.io.IOException;
import java.util.UUID;

@NoArgsConstructor
@ToString(callSuper = true)
public final class EntitySourceInfo extends SourceInfo {

    @Getter
    private int entityId;

    public EntitySourceInfo(@NotNull String addonId,
                            @NotNull UUID sourceId,
                            @NotNull UUID lineId,
                            @Nullable String sourceName,
                            byte state,
                            @Nullable CodecInfo decoderInfo,
                            boolean stereo,
                            boolean iconVisible,
                            int angle,
                            int entityId) {
        super(addonId, sourceId, lineId, sourceName, state, decoderInfo, stereo, iconVisible, angle);

        this.entityId = entityId;
    }

    @Override
    public void deserialize(ByteArrayDataInput in) throws IOException {
        super.deserialize(in);

        this.entityId = in.readInt();
    }

    @Override
    public void serialize(ByteArrayDataOutput out) throws IOException {
        super.serialize(out);

        out.writeInt(entityId);
    }

    @Override
    public Type getType() {
        return Type.ENTITY;
    }
}
