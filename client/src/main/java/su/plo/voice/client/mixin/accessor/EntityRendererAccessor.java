package su.plo.voice.client.mixin.accessor;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(EntityRenderer.class)
public interface EntityRendererAccessor {

    //#if MC>=12102
    //$$ @Invoker("shouldShowName")
    //$$ boolean shouldShowName(Entity entity, double distanceToCamera);
    //#else
    @Invoker("shouldShowName")
    boolean shouldShowName(Entity entity);
    //#endif
}
