package su.plo.voice.proto.data.audio.capture;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.proto.data.audio.codec.CodecInfo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Base interface for activations.
 */
public interface Activation {

    /**
     * Gets the unique identifier of the activation.
     *
     * @return The activation id.
     */
    @NotNull UUID getId();

    /**
     * Gets the name of the activation.
     *
     * @return The activation name.
     */
    @NotNull String getName();

    /**
     * Gets the translation key associated with the activation.
     *
     * @return The activation's key string.
     */
    @NotNull String getTranslation();

    /**
     * Gets the icon of the activation, which can be a Minecraft ResourceLocation or base64-encoded data.
     *
     * @return The activation's icon.
     */
    @NotNull String getIcon();

    /**
     * Gets a collection of distances associated with the activation.
     *
     * @return A list of activation distances.
     */
    List<Integer> getDistances();

    /**
     * Gets the default distance for the activation.
     *
     * @return The default activation distance.
     */
    int getDefaultDistance();

    /**
     * Gets the minimum distance from the list of distances.
     *
     * @return The minimum activation distance.
     */
    int getMinDistance();

    /**
     * Gets the maximum distance from the list of distances.
     *
     * @return The maximum activation distance.
     */
    int getMaxDistance();

    /**
     * Checks if the activation has proximity output.
     *
     * <p>
     *     This can be used by addons to create unique behavior.
     * </p>
     *
     * <p>
     *     For example in <a href="https://github.com/plasmoapp/pv-addon-soundphysics">pv-addon-soundphysics</a>
     *     it's used to create reverb from your capture only for activations with proximity output.
     * </p>
     *
     * @return {@code true} if the activation has proximity output, {@code false} otherwise.
     */
    boolean isProximity();

    /**
     * Checks if the activation is transitive.
     *
     * <p>
     *     If an activation is NOT transitive, subsequent activations will NOT be triggered.
     * </p>
     *
     * @return {@code true} if the activation is transitive, {@code false} otherwise.
     */
    boolean isTransitive();

    /**
     /**
     * Checks if stereo audio is supported by the activation.
     *
     * <p>
     *     If enabled, the client will send stereo audio if it's configured in the client settings.
     * </p>
     *
     * @return {@code true} if stereo audio is supported by the activation, {@code false} otherwise.
     */
    default boolean isStereoSupported() {
        return false;
    }

    /**
     * Gets encoder information for the activation.
     *
     * <p>
     *     If not null, the client will use this information to encode audio data.
     * </p>
     *
     * @return An optional containing the encoder information, if available.
     */
    Optional<CodecInfo> getEncoderInfo();

    /**
     * Gets the weight of the activation.
     *
     * <p>
     *     The weight determines the order of source lines in a client-side menu and overlay.
     *     A lower weight indicates a higher priority.
     * </p>
     *
     * @return The weight of the activation.
     */
    default int getWeight() {
        return 0;
    }

    /**
     * Checks if a specified distance is among the available distances for the activation.
     *
     * <p>
     *     If distances are empty, returns {@code true}.
     * </p>
     * <p>
     *     If distances are dynamic, returns {@code true} if the specified distance is within the range (0, {@link #getMaxDistance()}].
     * </p>
     *
     * @param distance The distance to check.
     * @return {@code true} if the specified distance is among the available distances, {@code false} otherwise.
     */
    default boolean checkDistance(int distance) {
        List<Integer> distances = getDistances();

        if (distances.isEmpty()) {
            return true;
        }

        if (distances.size() == 2 && distances.get(0) == -1) {
            return distance >= 1 && distance <= distances.get(1);
        }

        return distances.contains(distance);
    }

    /**
     * Calculates allowed distance.
     *
     * @param distance The distance to check.
     *
     * @return The provided distance if {@link #checkDistance(int)} is {@code true} {@link #getDefaultDistance()} otherwise.
     */
    default int calculateAllowedDistance(int distance) {
        if (checkDistance(distance)) {
            return distance;
        } else {
            return getDefaultDistance();
        }
    }
}
