package su.plo.voice.proto.data.source;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.proto.data.capture.VoiceActivation;

import java.util.UUID;

@NoArgsConstructor
@ToString(callSuper = true)
public final class EntitySourceInfo extends SourceInfo {

    @Getter
    private int entityId;

    public EntitySourceInfo(@NotNull String addonId,
                            @NotNull UUID sourceId,
                            byte state,
                            @NotNull String codec,
                            boolean stereo,
                            boolean iconVisible,
                            int angle,
                            int entityId) {
        super(addonId, sourceId, state, codec, stereo, VoiceActivation.PROXIMITY_ID, iconVisible, angle);

        this.entityId = entityId;
    }

    @Override
    public void deserialize(ByteArrayDataInput in) {
        super.deserialize(in);

        this.entityId = in.readInt();
    }

    @Override
    public void serialize(ByteArrayDataOutput out) {
        super.serialize(out);

        out.writeInt(entityId);
    }

    @Override
    public Type getType() {
        return Type.ENTITY;
    }
}
