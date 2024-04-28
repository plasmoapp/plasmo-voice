package su.plo.voice.client.event.render;

import su.plo.slib.api.position.Pos3d;
import gg.essential.universal.UMatrixStack;
import lombok.Getter;
import lombok.NonNull;
import net.minecraft.client.multiplayer.ClientLevel;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.event.Event;
import su.plo.voice.client.render.ModCamera;

public final class LevelRenderEvent implements Event {

    @Getter
    private final UMatrixStack stack;
    @Getter
    private final ClientLevel level;
    @Getter
    private final ModCamera camera;
    @Getter
    private final LightSupplier lightSupplier;
    @Getter
    private final float delta;

    public LevelRenderEvent(@NonNull UMatrixStack stack,
                            @NonNull ClientLevel level,
                            @NonNull ModCamera camera,
                            @NonNull LightSupplier lightSupplier,
                            float delta) {
        this.stack = stack;
        this.level = level;
        this.camera = camera;
        this.lightSupplier = lightSupplier;
        this.delta = delta;
    }

    public interface LightSupplier {

        int getLight(@NotNull Pos3d blockPos);
    }
}
