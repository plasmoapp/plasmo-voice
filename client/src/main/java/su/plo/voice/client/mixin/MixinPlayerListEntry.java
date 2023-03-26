package su.plo.voice.client.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import su.plo.voice.client.render.cape.DeveloperCapeManager;

@Mixin(PlayerInfo.class)
public abstract class MixinPlayerListEntry {

    @Shadow @Final private GameProfile profile;

    @Shadow private boolean pendingTextures;

    @Inject(method = "registerTextures", at = @At("HEAD"))
    private void loadTextures(CallbackInfo ci) {
        if (pendingTextures) return;

        DeveloperCapeManager.INSTANCE.registerTextures(profile.getName());
    }

    @Inject(method = "getCapeLocation", at = @At("TAIL"), cancellable = true)
    private void getCapeLocation(CallbackInfoReturnable<ResourceLocation> cir) {
        ResourceLocation cape = DeveloperCapeManager.INSTANCE.getCapeLocation(profile.getName());
        if (cape != null) cir.setReturnValue(cape);
    }
}
