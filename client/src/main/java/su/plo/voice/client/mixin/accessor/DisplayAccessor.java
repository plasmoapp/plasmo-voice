package su.plo.voice.client.mixin.accessor;

import net.minecraft.world.entity.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Display.class)
public interface DisplayAccessor {
    @Accessor("interpolationStartClientTick")
    long plasmovoice_getInterpolationStartClientTick();

    @Accessor("interpolationDuration")
    int plasmovoice_getInterpolationDuration();
}
