package su.plo.voice.client.event.render;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import lombok.NonNull;
import net.minecraft.world.entity.Entity;
import su.plo.voice.api.event.Event;
import su.plo.voice.client.render.ModCamera;

public final class EntityRenderEvent implements Event {

    @Getter
    private final PoseStack stack;
    @Getter
    private final ModCamera camera;
    @Getter
    private final Entity entity;
    @Getter
    private final int light;
    private final boolean label;

    public EntityRenderEvent(@NonNull PoseStack stack,
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
