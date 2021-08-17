package su.plo.voice.mixin;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import su.plo.voice.client.VoiceClient;
import su.plo.voice.gui.PlayerVolumeHandler;

@Mixin(Keyboard.class)
public abstract class MixinKeyboard {
    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(at = @At("HEAD"), method = "onKey", cancellable = true)
    private void onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        InputUtil.Key volumeKey = KeyBindingHelper.getBoundKeyOf(VoiceClient.volumeKey);
        if (window == this.client.getWindow().getHandle() &&
                volumeKey.getCategory().equals(InputUtil.Type.KEYSYM) &&
                volumeKey.getCode() == key) {
            if (PlayerVolumeHandler.onButton(action)) {
                ci.cancel();
            }
        }
    }
}
