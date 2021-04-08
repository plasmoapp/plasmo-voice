package su.plo.voice.mixin;

import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import su.plo.voice.client.VoiceClient;

@Mixin(Mouse.class)
public abstract class MixinMouse {
    @Inject(at = @At("RETURN"), method = "onMouseButton", cancellable = true)
    private void onMouseButton(long window, int button, int action, int mods, CallbackInfo info) {
        if(action == 1) {
            VoiceClient.mouseKeyPressed.add(button);
        } else {
            VoiceClient.mouseKeyPressed.remove(button);
        }
    }
}
