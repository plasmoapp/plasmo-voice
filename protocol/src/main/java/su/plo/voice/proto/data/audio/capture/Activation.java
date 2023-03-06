package su.plo.voice.proto.data.audio.capture;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.proto.data.audio.codec.CodecInfo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface Activation {

    /**
     * @return the activation id
     */
    @NotNull UUID getId();

    /**
     * @return the activation name
     */
    @NotNull String getName();

    /**
     * @return the activation's translation string
     */
    @NotNull String getTranslation();

    /**
     * Gets the activation's icon
     *
     * @return minecraft's ResourceLocation or base64 in format: "base64;base64_string
     */
    @NotNull String getIcon();

    /**
     * @return collection of activation's distances
     */
    List<Integer> getDistances();

    /**
     * @return the default distance
     */
    int getDefaultDistance();

    /**
     * Gets the min distance from the distances collection
     *
     * @return the min distance
     */
    int getMinDistance();

    /**
     * Gets the max distance from the distances collection
     *
     * @return the max distance
     */
    int getMaxDistance();

    /**
     * Checks if activation has proximity output
     *
     * <p>
     *     This can be used by addons to create unique behavior
     * </p>
     *
     * <p>
     *     For example in <a href="https://github.com/plasmoapp/pv-addon-soundphysics">pv-addon-soundphysics</a>
     *     it's used to create reverb from your capture only for activations with proximity output
     * </p>
     *
     * @return true if activation has proximity output
     */
    boolean isProximity();

    /**
     * Checks if activation is transitive
     *
     * <p>
     *     If activation is NOT transitive, then all subsequent activations will NOT be activated
     * </p>
     *
     * @return true if activation is transitive
     */
    boolean isTransitive();

    /**
     * Checks if stereo is supported
     *
     * <p>
     *     Client will send a stereo audio if it's enabled in the client settings
     * </p>
     *
     * @return true if stereo is supported
     */
    default boolean isStereoSupported() {
        return false;
    }

    /**
     * Gets encoder info
     *
     * <p>
     *     If encoder info is not null, client will use this info to encode audio data
     * </p>
     */
    Optional<CodecInfo> getEncoderInfo();

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
