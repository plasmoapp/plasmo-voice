package su.plo.voice.client.mixin;

import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import su.plo.voice.client.ModVoiceClient;
import su.plo.voice.client.crowdin.PlasmoCrowdinMod;
import su.plo.voice.client.crowdin.PlasmoCrowdinPack;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Mixin(ReloadableResourceManager.class)
public abstract class MixinReloadableResourceManager {

    @Shadow @Final private PackType type;

    @ModifyArg(
            method = "createReload",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/packs/resources/MultiPackResourceManager;<init>(Lnet/minecraft/server/packs/PackType;Ljava/util/List;)V"
            ),
            index = 1
    )
    private List<PackResources> onPostReload(List<PackResources> packs) {
        //#if FORGE
        //$$ try {
        //$$     Class.forName("kotlin.jvm.internal.Intrinsics");
        //$$ } catch (Exception ignored) {
        //$$     return packs;
        //$$ }
        //#endif

        if (this.type != PackType.CLIENT_RESOURCES) return packs;

        List<PackResources> list = new ArrayList<>(packs);
        list.add(new PlasmoCrowdinPack(
                new File(new File("config/plasmovoice"), PlasmoCrowdinMod.INSTANCE.getFolderName()))
        );
        return list;
    }
}
