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
import su.plo.voice.proto.packets.PacketSerializable;
import su.plo.voice.proto.packets.PacketUtil;

import java.util.List;
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
                           int weight) {
        this.name = checkNotNull(name);
        this.translation = translation;
        this.icon = checkNotNull(icon);
        this.id = generateId(name);
        this.distances = checkNotNull(distances);
        this.defaultDistance = defaultDistance;
        this.proximity = proximity;
        this.transitive = transitive;
        this.weight = weight;
        this.stereoSupported = stereoSupported;
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
    public void deserialize(ByteArrayDataInput in) {
        this.name = in.readUTF();
        this.translation = in.readUTF();
        this.icon = in.readUTF();
        this.id = VoiceActivation.generateId(name);
        this.distances = PacketUtil.readIntList(in);
        this.defaultDistance = in.readInt();
        this.proximity = in.readBoolean();
        this.transitive = in.readBoolean();
        this.weight = in.readInt();
        this.stereoSupported = in.readBoolean();
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
        out.writeInt(weight);
        out.writeBoolean(stereoSupported);
    }
}
