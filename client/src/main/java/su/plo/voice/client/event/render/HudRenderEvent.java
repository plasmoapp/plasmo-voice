package su.plo.voice.client.event.render;

import su.plo.voice.universal.UMatrixStack;
import lombok.Getter;
import lombok.NonNull;
import su.plo.voice.api.event.Event;

public final class HudRenderEvent implements Event {

    @Getter
    private final UMatrixStack stack;
    @Getter
    private final float delta;

    public HudRenderEvent(@NonNull UMatrixStack stack,
                          float delta) {
        this.stack = stack;
        this.delta = delta;
    }
}
