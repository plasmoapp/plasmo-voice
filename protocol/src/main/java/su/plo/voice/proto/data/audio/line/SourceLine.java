package su.plo.voice.proto.data.audio.line;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.slib.api.entity.player.McGameProfile;

import java.util.Collection;
import java.util.UUID;

/**
 * Represents a base source line.
 */
public interface SourceLine {

    /**
     * Gets the source line's unique identifier.
     *
     * @return The source line unique identifier.
     */
    @NotNull UUID getId();

    /**
     * Gets the name of the source line.
     *
     * @return The source line name.
     */
    @NotNull String getName();

    /**
     * Gets the translation key associated with the source line.
     *
     * @return The translation key.
     */
    @NotNull String getTranslation();

    /**
     * Gets the icon associated with the source line.
     *
     * @return The source line icon, represented as a Minecraft ResourceLocation or a base64-encoded string.
     */
    @NotNull String getIcon();

    /**
     * Gets the default volume level for the source line.
     *
     * @return The default volume level.
     */
    double getDefaultVolume();

    /**
     * Gets the weight of the source line.
     *
     * <p>
     *     The weight determines the order of source lines in a client-side menu and overlay.
     *     A lower weight indicates a higher priority.
     * </p>
     *
     * @return The source line weight
     */
    default int getWeight() {
        return 0;
    }

    /**
     * Checks if the source line can contain players.
     *
     * @return {@code true} if the source line can contain players, {@code false} otherwise.
     */
    boolean hasPlayers();

    /**
     * Gets the players associated with the source line.
     *
     * @return A collection of {@link McGameProfile} representing the players in the source line,
     * or null if {@link #hasPlayers()} is {@code false}.
     */
    @Nullable Collection<McGameProfile> getPlayers();
}
