package su.plo.voice.client.mixin;


import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyboardHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import su.plo.voice.client.VoiceClientForge;

@Mixin(KeyboardHandler.class)
public abstract class MixinKeyboardHandler {
    @Inject(at = @At("HEAD"), method = "keyPress", cancellable = true)
    private void onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (action == 1) {
            VoiceClientForge.getClientConfig().keyBindings.onKeyDown(InputConstants.Type.KEYSYM.getOrCreate(key));
        } else {
            VoiceClientForge.getClientConfig().keyBindings.onKeyUp(InputConstants.Type.KEYSYM.getOrCreate(key));
        }
    }
}
