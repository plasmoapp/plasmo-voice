package su.plo.lib.api.client.event.render;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.client.gui.GuiRender;
import su.plo.lib.api.client.render.MinecraftCamera;
import su.plo.lib.api.entity.MinecraftEntity;
import su.plo.voice.api.event.Event;

import static com.google.common.base.Preconditions.checkNotNull;

public final class EntityRenderEvent implements Event {

    @Getter
    private final GuiRender render;
    @Getter
    private final MinecraftCamera camera;
    @Getter
    private final int light;
    @Getter
    private final MinecraftEntity entity;
    private final boolean label;

    public EntityRenderEvent(@NotNull GuiRender render,
                             @NotNull MinecraftCamera camera,
                             int light,
                             @NotNull MinecraftEntity entity,
                             boolean label) {
        this.render = checkNotNull(render);
        this.camera = checkNotNull(camera);
        this.light = light;
        this.entity = entity;
        this.label = label;
    }

    public boolean hasLabel() {
        return label;
    }
}
