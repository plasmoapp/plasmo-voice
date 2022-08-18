package su.plo.voice.proto.data.source;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@NoArgsConstructor
@ToString
public final class EntitySourceInfo extends SourceInfo {

    @Getter
    private int entityId;

    public EntitySourceInfo(@NotNull UUID sourceId, @NotNull String codec, boolean iconVisible, int angle, int entityId) {
        super(sourceId, codec, iconVisible, angle);

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
