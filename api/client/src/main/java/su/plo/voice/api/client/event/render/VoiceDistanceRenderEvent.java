package su.plo.voice.api.client.event.render;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.client.render.DistanceVisualizer;
import su.plo.voice.api.event.EventCancellableBase;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This event is fired when the {@link DistanceVisualizer} is about to render a voice distance sphere.
 */
public final class VoiceDistanceRenderEvent extends EventCancellableBase {

    @Getter
    private final DistanceVisualizer visualizer;
    @Getter
    @Setter
    private int radius;
    @Getter
    @Setter
    private int color;

    public VoiceDistanceRenderEvent(@NotNull DistanceVisualizer visualizer, int radius, int color) {
        this.visualizer = checkNotNull(visualizer, "visualizer cannot be null");
        this.radius = radius;
        this.color = color;
    }
}
