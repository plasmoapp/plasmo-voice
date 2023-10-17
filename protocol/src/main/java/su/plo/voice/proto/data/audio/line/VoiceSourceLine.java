package su.plo.voice.proto.data.audio.line;

import com.google.common.base.Charsets;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.slib.api.entity.player.McGameProfile;
import su.plo.voice.proto.packets.PacketSerializable;
import su.plo.voice.proto.serializer.McGameProfileSerializer;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class VoiceSourceLine implements SourceLine, PacketSerializable {

    public static final String PROXIMITY_NAME = "proximity";
    public static final UUID PROXIMITY_ID = generateId(PROXIMITY_NAME);

    public static UUID generateId(@NotNull String name) {
        return UUID.nameUUIDFromBytes((name + "_line").getBytes(Charsets.UTF_8));
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
    @Getter
    protected double defaultVolume;
    @Getter
    protected int weight;
    protected Set<McGameProfile> players = null;

    public VoiceSourceLine(@NotNull String name,
                           @NotNull String translation,
                           @NotNull String icon,
                           double defaultVolume,
                           int weight,
                           @Nullable Set<McGameProfile> players) {
        this.id = generateId(name);
        this.name = checkNotNull(name);
        this.translation = translation;
        this.icon = checkNotNull(icon);
        this.defaultVolume = Math.max(Math.min(defaultVolume, 1D), 0D);
        this.weight = weight;
        this.players = players;
    }

    @Override
    public void deserialize(ByteArrayDataInput in) throws IOException {
        this.name = in.readUTF();
        this.id = generateId(name);
        this.translation = in.readUTF();
        this.icon = in.readUTF();
        this.defaultVolume = in.readDouble();
        this.weight = in.readInt();
        if (in.readBoolean()) {
            this.players = new HashSet<>();
            int size = in.readInt();
            for (int i = 0; i < size; i++) {
                players.add(McGameProfileSerializer.INSTANCE.deserialize(in));
            }
        }
    }

    @Override
    public void serialize(ByteArrayDataOutput out) throws IOException {
        out.writeUTF(name);
        out.writeUTF(translation);
        out.writeUTF(icon);
        out.writeDouble(defaultVolume);
        out.writeInt(weight);
        out.writeBoolean(hasPlayers());
        if (hasPlayers()) {
            out.writeInt(players.size());

            for (McGameProfile player : players) {
                McGameProfileSerializer.INSTANCE.serialize(player, out);
            }
        }
    }

    @Override
    public boolean hasPlayers() {
        return players != null;
    }

    @Override
    public @Nullable Collection<McGameProfile> getPlayers() {
        return players;
    }
}
