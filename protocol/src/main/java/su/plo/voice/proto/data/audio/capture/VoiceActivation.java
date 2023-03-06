package su.plo.voice.proto.data.audio.capture;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.proto.data.audio.codec.CodecInfo;
import su.plo.voice.proto.packets.PacketSerializable;
import su.plo.voice.proto.packets.PacketUtil;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class VoiceActivation implements Activation, PacketSerializable {

    public static final String PROXIMITY_NAME = "proximity";
    public static final UUID PROXIMITY_ID = generateId(PROXIMITY_NAME);

    public static UUID generateId(@NotNull String name) {
        return UUID.nameUUIDFromBytes((name + "_activation").getBytes(Charsets.UTF_8));
    }

    @Getter
    @EqualsAndHashCode.Include
    protected UUID id;
    @Getter
    protected String name;
    @Getter
    protected String translation;
    @Getter
    protected String icon;
    protected List<Integer> distances = ImmutableList.of();
    @Getter
    protected int defaultDistance;
    @Getter
    protected boolean proximity;
    @Getter
    protected boolean transitive;
    @Getter
    protected boolean stereoSupported;
    protected @Nullable CodecInfo encoderInfo;
    @Getter
    protected int weight;

    public VoiceActivation(@NotNull String name,
                           @NotNull String translation,
                           @NotNull String icon,
                           List<Integer> distances,
                           int defaultDistance,
                           boolean proximity,
                           boolean stereoSupported,
                           boolean transitive,
                           @Nullable CodecInfo encoderInfo,
                           int weight) {
        this.name = checkNotNull(name);
        this.translation = translation;
        this.icon = checkNotNull(icon);
        this.id = generateId(name);
        this.distances = checkNotNull(distances);
        this.defaultDistance = defaultDistance;
        this.proximity = proximity;
        this.stereoSupported = stereoSupported;
        this.transitive = transitive;
        this.encoderInfo = encoderInfo;
        this.weight = weight;
    }

    @Override
    public List<Integer> getDistances() {
        return distances;
    }

    @Override
    public int getMinDistance() {
        if (distances.size() == 0) return -1;
        return distances.get(0);
    }

    @Override
    public int getMaxDistance() {
        if (distances.size() == 0) return -1;
        return distances.get(distances.size() - 1);
    }

    @Override
    public Optional<CodecInfo> getEncoderInfo() {
        return Optional.ofNullable(encoderInfo);
    }

    @Override
    public void deserialize(ByteArrayDataInput in) {
        this.name = in.readUTF();
        this.translation = in.readUTF();
        this.icon = in.readUTF();
        this.id = VoiceActivation.generateId(name);
        this.distances = PacketUtil.readIntList(in);
        this.defaultDistance = in.readInt();
        this.proximity = in.readBoolean();
        this.transitive = in.readBoolean();
        this.stereoSupported = in.readBoolean();
        if (in.readBoolean()) {
            this.encoderInfo = new CodecInfo();
            encoderInfo.deserialize(in);
        }
        this.weight = in.readInt();
    }

    @Override
    public void serialize(ByteArrayDataOutput out) {
        out.writeUTF(name);
        out.writeUTF(translation);
        out.writeUTF(icon);
        PacketUtil.writeIntList(out, distances);
        out.writeInt(defaultDistance);
        out.writeBoolean(proximity);
        out.writeBoolean(transitive);
        out.writeBoolean(stereoSupported);
        out.writeBoolean(encoderInfo != null);
        if (encoderInfo != null) encoderInfo.serialize(out);
        out.writeInt(weight);
    }
}
