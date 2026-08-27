package su.plo.voice.client.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import su.plo.voice.client.render.cape.DeveloperCapeManager;

import net.minecraft.client.resources.PlayerSkin;

@Mixin(PlayerInfo.class)
public abstract class MixinPlayerListEntry {

    @Shadow @Final private GameProfile profile;

    @Inject(method = "getSkin", at = @At("RETURN"), cancellable = true)
    private void getSkin(CallbackInfoReturnable<PlayerSkin> cir) {
        ResourceLocation cape = DeveloperCapeManager.INSTANCE.getCapeLocation(profile.getName());
        if (cape == null) return;
        PlayerSkin skin = DeveloperCapeManager.INSTANCE.addCapeToSkin(
                profile.getName(),
                cape,
                cir.getReturnValue()
        );
        cir.setReturnValue(skin);
    }
}
