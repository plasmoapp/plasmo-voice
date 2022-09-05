package su.plo.voice.proto.data.capture;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
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
     * Gets the activation's hud icon resource location
     *
     * @return the activation's icon resource location
     */
    @NotNull String getHudIconLocation();

    /**
     * Gets the activation's source icon resource location
     *
     * @return the activation's source resource location
     */
    @NotNull String getSourceIconLocation();

    /**
     * Gets the activation's available distances
     *
     * @return collection of distances
     */
    Collection<Integer> getDistances();

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
     *
     * todo: doc
     *
     * @return true if activation is transitive
     */
    boolean isTransitive();

    /**
     * Gets the activation's weight
     *
     * todo: doc
     *
     * @return the weight
     */
    default int getWeight() {
        return 0;
    };
}
