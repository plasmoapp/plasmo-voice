package su.plo.voice.proto.data.source;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import su.plo.voice.proto.packets.PacketSerializable;
import su.plo.voice.proto.packets.PacketUtil;

import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

@AllArgsConstructor
@NoArgsConstructor
@ToString
public abstract class SourceInfo implements PacketSerializable {

    public static SourceInfo of(ByteArrayDataInput in) {
        SourceInfo sourceInfo = null;
        switch (Type.valueOf(in.readUTF())) {
            case PLAYER:
                sourceInfo = new PlayerSourceInfo();
                break;
            case ENTITY:
                return null;
            case STATIC:
                return null;
            case DIRECT:
                return null;
        }

        if (sourceInfo == null) throw new IllegalArgumentException("Invalid source type");

        sourceInfo.deserialize(in);
        return sourceInfo;
    }

    @Getter
    protected UUID sourceId;
    @Getter
    protected boolean iconVisible;

    @Override
    public void deserialize(ByteArrayDataInput in) {
        this.sourceId = PacketUtil.readUUID(in);
        this.iconVisible = in.readBoolean();
    }

    @Override
    public void serialize(ByteArrayDataOutput out) {
        PacketUtil.writeUUID(out, checkNotNull(sourceId));
        out.writeBoolean(iconVisible);
    }

    public abstract Type getType();

    public enum Type {
        PLAYER,
        ENTITY,
        STATIC,
        DIRECT
    }
}
