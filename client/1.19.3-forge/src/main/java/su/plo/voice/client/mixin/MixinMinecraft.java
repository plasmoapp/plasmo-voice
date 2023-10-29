package su.plo.voice.client.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import su.plo.voice.client.ModVoiceClient;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft {

    @Shadow public abstract boolean isRunning();

    @Inject(method = "stop", at = @At("HEAD"))
    public void stop(CallbackInfo ci) {
        if (!isRunning()) return;
        ModVoiceClient.INSTANCE.onShutdown();
    }
}
