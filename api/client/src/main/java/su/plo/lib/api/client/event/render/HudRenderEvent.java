package su.plo.lib.api.client.event.render;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.client.gui.GuiRender;
import su.plo.voice.api.event.Event;

import static com.google.common.base.Preconditions.checkNotNull;

public final class HudRenderEvent implements Event {

    @Getter
    private final GuiRender render;
    @Getter
    private final float delta;

    public HudRenderEvent(@NotNull GuiRender render, float delta) {
        this.render = checkNotNull(render);
        this.delta = delta;
    }
}
