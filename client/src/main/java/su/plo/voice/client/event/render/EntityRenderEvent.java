package su.plo.voice.client.event.render;

import su.plo.voice.universal.UMatrixStack;
import lombok.Getter;
import lombok.NonNull;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import su.plo.voice.api.event.Event;
import su.plo.voice.client.render.ModCamera;

public final class EntityRenderEvent implements Event {

    @Getter
    private final UMatrixStack stack;
    @Getter
    private final ModCamera camera;
    @Getter
    private final Entity entity;
    @Getter
    private final int light;
    private final boolean label;

    public EntityRenderEvent(@NonNull UMatrixStack stack,
                             @NonNull ModCamera camera,
                             @NonNull Entity entity,
                             int light,
                             boolean label) {
        this.stack = stack;
        this.camera = camera;
        this.light = light;
        this.entity = entity;
        this.label = label;
    }

    public boolean hasLabel() {
        return label;
    }
}
