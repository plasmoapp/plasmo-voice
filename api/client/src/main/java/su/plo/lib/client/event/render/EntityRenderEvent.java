package su.plo.lib.client.event.render;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.client.gui.GuiRender;
import su.plo.lib.client.render.MinecraftCamera;
import su.plo.lib.entity.MinecraftEntity;
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

    public EntityRenderEvent(@NotNull GuiRender render,
                             @NotNull MinecraftCamera camera,
                             int light,
                             @NotNull MinecraftEntity entity) {
        this.render = checkNotNull(render);
        this.camera = checkNotNull(camera);
        this.light = light;
        this.entity = entity;
    }
}
