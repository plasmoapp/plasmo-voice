package su.plo.voice.proto.data.capture;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.proto.packets.PacketSerializable;
import su.plo.voice.proto.packets.PacketUtil;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

// todo (both-sided):
//  name = translation key
//  transitive
//  priority
//  distances:
//  - [1, 2, 4] -> just a slider
//  - [-1, 100] -> input with max value

// todo (client-sided):
//  ptt key
//  toggle button
//  activation types (by default its ptt):
//  - ptt -> key
//  - voice -> toggle
@NoArgsConstructor
@ToString
public class VoiceActivation implements Activation, PacketSerializable {

    public static UUID generateId(@NotNull String name) {
        return UUID.nameUUIDFromBytes((name).getBytes(Charsets.UTF_8));
    }

    @Getter
    protected UUID id;
    @Getter
    protected String name;
    @Getter
    protected String translation;
    protected List<Integer> distances = ImmutableList.of();
    @Getter
    protected int defaultDistance;
    @Getter
    protected boolean transitive = true;
    @Getter
    protected Order priority;

    public VoiceActivation(@NotNull String name, @NotNull String translation, List<Integer> distances, int defaultDistance, Order priority) {
        this.name = checkNotNull(name);
        this.translation = translation;
        this.id = generateId(name);
        this.distances = checkNotNull(distances);
        this.defaultDistance = defaultDistance;
        this.priority = priority;
    }

    @Override
    public Collection<Integer> getDistances() {
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
        this.id = UUID.nameUUIDFromBytes((name).getBytes(Charsets.UTF_8));
        this.distances = PacketUtil.readIntList(in);
        this.defaultDistance = in.readInt();
        this.priority = Order.valueOf(in.readUTF());
    }

    @Override
    public void serialize(ByteArrayDataOutput out) {
        out.writeUTF(name);
        PacketUtil.writeIntList(out, distances);
        out.writeInt(defaultDistance);
        out.writeUTF(priority.name());
    }
}
