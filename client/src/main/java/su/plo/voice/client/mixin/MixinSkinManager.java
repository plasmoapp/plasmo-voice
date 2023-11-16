package su.plo.voice.client.mixin;

import net.minecraft.client.resources.SkinManager;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import su.plo.voice.client.render.cape.SkinManagerAccessor;

import java.io.File;

//#if MC>=12002
//$$ import com.mojang.authlib.minecraft.MinecraftSessionService;
//$$ import net.minecraft.client.renderer.texture.TextureManager;
//$$
//$$ import org.spongepowered.asm.mixin.Unique;
//$$ import org.spongepowered.asm.mixin.injection.At;
//$$ import org.spongepowered.asm.mixin.injection.Inject;
//$$ import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//$$
//$$ import java.nio.file.Path;
//$$ import java.util.concurrent.Executor;
//#else
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
//#endif

@Mixin(SkinManager.class)
public abstract class MixinSkinManager implements SkinManagerAccessor {

    //#if MC>=12002
    //$$ @Unique
    //$$ private static File SKINS_DIRECTORY;
    //$$ @Inject(at = @At("RETURN"), method = "<init>")
    //$$ private void init(TextureManager textureManager, Path path, MinecraftSessionService minecraftSessionService, Executor executor, CallbackInfo ci) {
    //$$     SKINS_DIRECTORY = path.toFile();
    //$$ }
    //$$ @NotNull
    //$$ @Override
    //$$ public File getSkinsCacheFolder() {
    //$$     return SKINS_DIRECTORY;
    //$$ }
    //#else
    @Shadow @Final private File skinsDirectory;

    @NotNull
    @Override
    public File getSkinsCacheFolder() {
        return skinsDirectory;
    }
    //#endif
}
