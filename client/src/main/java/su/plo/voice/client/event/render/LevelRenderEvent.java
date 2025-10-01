package su.plo.voice.client.event.render;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import lombok.NonNull;
import net.minecraft.client.multiplayer.ClientLevel;
import org.jetbrains.annotations.NotNull;
import su.plo.slib.api.position.Pos3d;
import su.plo.voice.api.event.Event;

public final class LevelRenderEvent implements Event {

    @Getter
    private final PoseStack stack;
    @Getter
    private final ClientLevel level;
    @Getter
    private final LightSupplier lightSupplier;
    @Getter
    private final float delta;

    public LevelRenderEvent(
            @NonNull PoseStack stack,
            @NonNull ClientLevel level,
            @NonNull LightSupplier lightSupplier,
            float delta
    ) {
        this.stack = stack;
        this.level = level;
        this.lightSupplier = lightSupplier;
        this.delta = delta;
    }

    public interface LightSupplier {

        int getLight(@NotNull Pos3d blockPos);
    }
}
