package su.plo.lib.client.event.render;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.client.gui.GuiRender;
import su.plo.lib.client.render.MinecraftCamera;
import su.plo.voice.api.event.Event;
import su.plo.voice.proto.data.pos.Pos3d;

import static com.google.common.base.Preconditions.checkNotNull;

public final class LevelRenderEvent implements Event {

    @Getter
    private final GuiRender render;
    @Getter
    private final MinecraftCamera camera;
    @Getter
    private final LightSupplier lightSupplier;
    @Getter
    private final float delta;

    public LevelRenderEvent(@NotNull GuiRender render,
                            @NotNull MinecraftCamera camera,
                            @NotNull LightSupplier lightSupplier,
                            float delta) {
        this.render = checkNotNull(render);
        this.camera = checkNotNull(camera);
        this.lightSupplier = lightSupplier;
        this.delta = delta;
    }

    public interface LightSupplier {

        int getLight(@NotNull Pos3d blockPos);
    }
}
