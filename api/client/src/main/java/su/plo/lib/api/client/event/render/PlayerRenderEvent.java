package su.plo.lib.api.client.event.render;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.client.gui.GuiRender;
import su.plo.lib.api.client.render.MinecraftCamera;
import su.plo.lib.api.entity.MinecraftPlayerEntity;
import su.plo.voice.api.event.Event;

import static com.google.common.base.Preconditions.checkNotNull;

public final class PlayerRenderEvent implements Event {

    @Getter
    private final GuiRender render;
    @Getter
    private final MinecraftCamera camera;
    @Getter
    private final int light;
    @Getter
    private final MinecraftPlayerEntity player;
    private final boolean label;
    @Getter
    private final boolean fakePlayer;

    public PlayerRenderEvent(@NotNull GuiRender render,
                             @NotNull MinecraftCamera camera,
                             @NotNull MinecraftPlayerEntity player,
                             int light,
                             boolean label,
                             boolean fakePlayer) {
        this.render = checkNotNull(render);
        this.camera = checkNotNull(camera);
        this.light = light;
        this.player = player;
        this.label = label;
        this.fakePlayer = fakePlayer;
    }

    public boolean hasLabel() {
        return label;
    }
}
