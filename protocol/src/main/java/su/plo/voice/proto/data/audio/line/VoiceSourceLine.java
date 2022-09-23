package su.plo.voice.proto.data.audio.line;

import com.google.common.base.Charsets;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.proto.packets.PacketSerializable;

import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

@NoArgsConstructor
@ToString
public class VoiceSourceLine implements SourceLine, PacketSerializable {

    public static final String PROXIMITY_NAME = "proximity";
    public static final UUID PROXIMITY_ID = generateId(PROXIMITY_NAME);

    public static UUID generateId(@NotNull String name) {
        return UUID.nameUUIDFromBytes((name + "_line").getBytes(Charsets.UTF_8));
    }

    @Getter
    protected UUID id;
    @Getter
    protected String name;
    @Getter
    protected String translation;
    @Getter
    protected String icon;
    @Getter
    protected int weight;

    public VoiceSourceLine(@NotNull String name,
                           @NotNull String translation,
                           @NotNull String icon,
                           int weight) {
        this.name = checkNotNull(name);
        this.translation = translation;
        this.icon = checkNotNull(icon);
        this.id = generateId(name);
        this.weight = weight;
    }

    @Override
    public void deserialize(ByteArrayDataInput in) {
        this.name = in.readUTF();
        this.id = generateId(name);
        this.translation = in.readUTF();
        this.icon = in.readUTF();
        this.weight = in.readInt();
    }

    @Override
    public void serialize(ByteArrayDataOutput out) {
        out.writeUTF(name);
        out.writeUTF(translation);
        out.writeUTF(icon);
        out.writeInt(weight);
    }
}
