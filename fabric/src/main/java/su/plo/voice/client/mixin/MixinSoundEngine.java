package su.plo.voice.client.mixin;

import com.mojang.blaze3d.audio.Library;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import su.plo.voice.client.VoiceClient;

@Mixin(Library.class)
public abstract class MixinSoundEngine {
    @Inject(method = "cleanup", at = @At("RETURN"))
    public void close(CallbackInfo ci) {
        VoiceClient.getSoundEngine().close();
    }
}
