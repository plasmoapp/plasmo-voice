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
import su.plo.voice.proto.data.player.MinecraftGameProfile;
import su.plo.voice.proto.packets.PacketSerializable;

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
    protected int weight;
    protected Set<MinecraftGameProfile> players = null;

    public VoiceSourceLine(@NotNull String name,
                           @NotNull String translation,
                           @NotNull String icon,
                           int weight,
                           @Nullable Set<MinecraftGameProfile> players) {
        this.name = checkNotNull(name);
        this.translation = translation;
        this.icon = checkNotNull(icon);
        this.id = generateId(name);
        this.weight = weight;
        this.players = players;
    }

    @Override
    public void deserialize(ByteArrayDataInput in) throws IOException {
        this.name = in.readUTF();
        this.id = generateId(name);
        this.translation = in.readUTF();
        this.icon = in.readUTF();
        this.weight = in.readInt();
        if (in.readBoolean()) {
            this.players = new HashSet<>();
            int size = in.readInt();
            for (int i = 0; i < size; i++) {
                MinecraftGameProfile player = new MinecraftGameProfile();
                player.deserialize(in);
                players.add(player);
            }
        }
    }

    @Override
    public void serialize(ByteArrayDataOutput out) throws IOException {
        out.writeUTF(name);
        out.writeUTF(translation);
        out.writeUTF(icon);
        out.writeInt(weight);
        out.writeBoolean(hasPlayers());
        if (hasPlayers()) {
            out.writeInt(players.size());
            for (MinecraftGameProfile player : players) {
                player.serialize(out);
            }
        }
    }

    @Override
    public boolean hasPlayers() {
        return players != null;
    }

    @Override
    public @NotNull Collection<MinecraftGameProfile> getPlayers() {
        return players;
    }
}
