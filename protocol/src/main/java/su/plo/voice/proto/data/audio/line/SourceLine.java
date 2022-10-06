package su.plo.voice.proto.data.audio.line;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.UUID;

public interface SourceLine {

    /**
     * Gets the line id
     *
     * @return the line id
     */
    @NotNull UUID getId();

    /**
     * Gets the line name
     *
     * @return the line name
     */
    @NotNull String getName();

    /**
     * Gets the line's translation string
     *
     * @return the line's translation string
     */
    @NotNull String getTranslation();

    /**
     * Gets the line's icon
     *
     * Minecraft ResourceLocation or base64 in format: "base64;<base64_string>"
     *
     * @return the line's icon
     */
    @NotNull String getIcon();

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
    @NotNull
    Collection<UUID> getPlayers();
}
