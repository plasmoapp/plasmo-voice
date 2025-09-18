package su.plo.voice.client.mixin;

import net.minecraft.client.resources.SkinManager;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import su.plo.voice.client.render.cape.SkinManagerAccessor;

import java.io.File;

//#if MC>=12002
//$$ import com.mojang.authlib.minecraft.MinecraftSessionService;
//$$
//$$ import org.spongepowered.asm.mixin.Unique;
//$$ import org.spongepowered.asm.mixin.injection.At;
//$$ import org.spongepowered.asm.mixin.injection.Inject;
//$$ import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//$$
//$$ import java.nio.file.Path;
//$$ import java.util.concurrent.Executor;

//#if MC<12104
//$$ import net.minecraft.client.renderer.texture.TextureManager;
//#endif

//#if MC>=12109
//$$ import net.minecraft.client.renderer.texture.SkinTextureDownloader;
//$$ import net.minecraft.server.Services;
//$$ import org.spongepowered.asm.mixin.Final;
//$$ import org.spongepowered.asm.mixin.Shadow;
//#endif

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
    //#if MC>=12109
    //$$ private void init(Path path, final Services services, SkinTextureDownloader skinTextureDownloader, Executor executor, CallbackInfo ci) {
    //#elseif MC>=12104
    //$$ private void init(Path path, MinecraftSessionService minecraftSessionService, Executor executor, CallbackInfo ci) {
    //#else
    //$$ private void init(TextureManager textureManager, Path path, MinecraftSessionService minecraftSessionService, Executor executor, CallbackInfo ci) {
    //#endif
    //$$     SKINS_DIRECTORY = path.toFile();
    //$$ }
    //$$ @NotNull
    //$$ @Override
    //$$ public File plasmovoice_skinsCacheFolder() {
    //$$     return SKINS_DIRECTORY;
    //$$ }

    //#if MC>=12109
    //$$ @Shadow @Final private SkinTextureDownloader skinTextureDownloader;
    //$$ @Override
    //$$ public @NotNull SkinTextureDownloader plasmovoice_skinTextureDownloader() {
    //$$     return skinTextureDownloader;
    //$$ }
    //#endif

    //#else
    @Shadow @Final private File skinsDirectory;

    @NotNull
    @Override
    public File plasmovoice_skinsCacheFolder() {
        return skinsDirectory;
    }
    //#endif
}
