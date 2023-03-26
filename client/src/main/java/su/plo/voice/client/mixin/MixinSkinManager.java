package su.plo.voice.client.mixin;

import net.minecraft.client.resources.SkinManager;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import su.plo.voice.client.render.cape.SkinManagerAccessor;

import java.io.File;

@Mixin(SkinManager.class)
public abstract class MixinSkinManager implements SkinManagerAccessor {

    @Shadow @Final private File skinsDirectory;

    @NotNull
    @Override
    public File getSkinsCacheFolder() {
        return skinsDirectory;
    }
}
