package su.plo.voice.api.client.render;

import org.jetbrains.annotations.Nullable;
import su.plo.voice.proto.data.pos.Pos3d;

public interface DistanceVisualizer {

    /**
     * Renders the sphere with specified HEX color
     *
     * @param radius sphere radius
     * @param color HEX color
     */
    void render(int radius, int color, @Nullable Pos3d position);
}
