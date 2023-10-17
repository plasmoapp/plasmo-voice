package su.plo.voice.proto.data.audio.source;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.proto.data.audio.codec.CodecInfo;
import su.plo.voice.proto.packets.PacketSerializable;
import su.plo.voice.proto.packets.PacketUtil;

import java.io.IOException;
import java.util.UUID;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;

@AllArgsConstructor
@NoArgsConstructor
@ToString
public abstract class SourceInfo implements PacketSerializable {

    public static SourceInfo of(ByteArrayDataInput in) throws IOException {
        Type type = Type.valueOf(in.readUTF());
        SourceInfo sourceInfo = type.createSourceInfo();
        sourceInfo.deserialize(in);
        return sourceInfo;
    }

    @Getter
    protected String addonId;
    @Getter
    protected UUID id;
    @Getter
    protected UUID lineId;
    @Getter
    protected @Nullable String name;
    @Getter
    protected byte state;
    @Getter
    protected @Nullable CodecInfo decoderInfo;
    @Getter
    protected boolean stereo;
    @Getter
    protected boolean iconVisible;
    @Getter
    protected int angle;

    @Override
    public void deserialize(ByteArrayDataInput in) throws IOException {
        this.addonId = in.readUTF();
        this.id = PacketUtil.readUUID(in);
        this.name = PacketUtil.readNullableString(in);
        this.state = in.readByte();
        if (in.readBoolean()) {
            this.decoderInfo = new CodecInfo();
            decoderInfo.deserialize(in);
        }
        this.stereo = in.readBoolean();
        this.lineId = PacketUtil.readUUID(in);
        this.iconVisible = in.readBoolean();
        this.angle = in.readInt();
    }

    @Override
    public void serialize(ByteArrayDataOutput out) throws IOException {
        out.writeUTF(getType().name());
        out.writeUTF(checkNotNull(addonId));
        PacketUtil.writeUUID(out, checkNotNull(id));
        PacketUtil.writeNullableString(out, name);
        out.writeByte(state);
        out.writeBoolean(decoderInfo != null);
        if (decoderInfo != null) {
            decoderInfo.serialize(out);
        }
        out.writeBoolean(stereo);
        PacketUtil.writeUUID(out, lineId);
        out.writeBoolean(iconVisible);
        out.writeInt(angle);
    }

    public abstract Type getType();

    public enum Type {
        PLAYER(PlayerSourceInfo::new),
        ENTITY(EntitySourceInfo::new),
        STATIC(StaticSourceInfo::new),
        DIRECT(DirectSourceInfo::new);

        private final Supplier<SourceInfo> sourceInfoSupplier;

        Type(Supplier<SourceInfo> sourceInfoSupplier) {
            this.sourceInfoSupplier = sourceInfoSupplier;
        }

        public SourceInfo createSourceInfo() {
            return sourceInfoSupplier.get();
        }
    }
}
