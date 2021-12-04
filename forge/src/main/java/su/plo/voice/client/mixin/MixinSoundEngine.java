package su.plo.voice.client.mixin;

import com.mojang.blaze3d.audio.Library;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import su.plo.voice.client.VoiceClient;
import su.plo.voice.client.VoiceClientForge;

@Mixin(Library.class)
public abstract class MixinSoundEngine {
    @Inject(method = "init", at = @At("RETURN"))
    private void init(CallbackInfo ci) {
        VoiceClient.getSoundEngine().init();
    }

    @Inject(method = "cleanup", at = @At("RETURN"))
    public void close(CallbackInfo ci) {
        VoiceClientForge.getSoundEngine().close();
    }
}
