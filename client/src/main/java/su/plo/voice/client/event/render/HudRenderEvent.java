package su.plo.voice.client.event.render;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import lombok.NonNull;
import su.plo.voice.api.event.Event;

public final class HudRenderEvent implements Event {

    @Getter
    private final PoseStack stack;
    @Getter
    private final float delta;

    public HudRenderEvent(
            @NonNull PoseStack stack,
            float delta
    ) {
        this.stack = stack;
        this.delta = delta;
    }
}
