package su.plo.voice.api.client.render;

import org.jetbrains.annotations.Nullable;
import su.plo.slib.api.position.Pos3d;

/**
 * Visualizes the voice distance.
 */
public interface DistanceVisualizer {

    /**
     * Renders a sphere with the specified HEX color.
     *
     * @param radius   The radius of the sphere.
     * @param color    The HEX color code.
     * @param position The optional position where the sphere should be rendered. If null, position of the local player is used.
     */
    void render(int radius, int color, @Nullable Pos3d position);
}
