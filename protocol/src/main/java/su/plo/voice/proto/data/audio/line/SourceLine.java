package su.plo.voice.proto.data.audio.line;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.proto.data.player.MinecraftGameProfile;

import java.util.Collection;
import java.util.UUID;

public interface SourceLine {

    /**
     * @return the source line id
     */
    @NotNull UUID getId();

    /**
     * @return the source line name
     */
    @NotNull String getName();

    /**
     * @return the source line translation string
     */
    @NotNull String getTranslation();

    /**
     * Gets the source line icon
     *
     * @return minecraft's ResourceLocation or base64 in format: "base64;base64_string
     */
    @NotNull String getIcon();

    /**
     * @return the source line default volume
     */
    double getDefaultVolume();

    /**
     * Gets the line's weight
     * <p>
     * todo: doc
     *
     * @return the weight
     */
    default int getWeight() {
        return 0;
    }

    /**
     * Check if line can contain players
     */
    boolean hasPlayers();

    /**
     * Gets the line's players
     *
     * @return the line's players
     */
    @Nullable Collection<MinecraftGameProfile> getPlayers();
}
