package su.plo.voice.proto.data.audio.capture;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public interface Activation {

    /**
     * Gets the activation id
     *
     * @return the activation id
     */
    @NotNull UUID getId();

    /**
     * Gets the activation name
     *
     * @return the activation name
     */
    @NotNull String getName();

    /**
     * Gets the activation's translation string
     *
     * @return the activation's translation string
     */
    @NotNull String getTranslation();

    /**
     * Gets the activation's icon
     *
     * Minecraft ResourceLocation or base64 in format: "base64;<base64_string>"
     *
     * @return the activation's icon
     */
    @NotNull String getIcon();

    /**
     * Gets the activation's available distances
     *
     * @return collection of distances
     */
    List<Integer> getDistances();

    /**
     * Gets the activation's default distance
     *
     * @return the default distance
     */
    int getDefaultDistance();

    /**
     * Gets the min distance from a distances collection
     *
     * @return the min distance
     */
    int getMinDistance();

    /**
     * Gets the max distance from a distances collection
     *
     * @return the max distance
     */
    int getMaxDistance();

    /**
     * Checks if activation is transitive
     * <p>
     * todo: doc
     *
     * @return true if activation is transitive
     */
    boolean isTransitive();

    /**
     * Checks if stereo is supported
     * <p>
     * Client will send a stereo audio if it's enabled in the client settings
     *
     * @return true if stereo is supported
     */
    default boolean isStereoSupported() {
        return false;
    }

    /**
     * Gets the activation's weight
     * <p>
     * todo: doc
     *
     * @return the weight
     */
    default int getWeight() {
        return 0;
    }
}
