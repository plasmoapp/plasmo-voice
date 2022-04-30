package su.plo.voice.mixin;


import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyboardHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import su.plo.voice.client.VoiceClient;

@Mixin(KeyboardHandler.class)
public abstract class MixinKeyboardHandler {
    @Inject(at = @At("RETURN"), method = "keyPress")
    private void onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (VoiceClient.getClientConfig() == null) return;

        InputConstants.Key inputKey = InputConstants.Type.KEYSYM.getOrCreate(key);
        if (action == 1) {
            VoiceClient.getClientConfig().keyBindings.onKeyDown(inputKey);
        } else if (action == 0) {
            VoiceClient.getClientConfig().keyBindings.onKeyUp(inputKey);
        }
    }
}
