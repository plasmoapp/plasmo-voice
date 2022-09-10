package su.plo.voice.api.client.render;

public interface DistanceVisualizer {

    /**
     * Renders the sphere with specified HEX color
     *
     * @param radius sphere radius
     * @param color HEX color
     */
    void render(int radius, int color);
}
