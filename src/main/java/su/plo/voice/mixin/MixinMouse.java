package su.plo.voice.mixin;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import su.plo.voice.client.VoiceClient;
import su.plo.voice.gui.PlayerVolumeHandler;

@Mixin(Mouse.class)
public abstract class MixinMouse {
    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(at = @At("HEAD"), method = "onMouseButton", cancellable = true)
    private void onMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
        InputUtil.Key key = KeyBindingHelper.getBoundKeyOf(VoiceClient.volumeKey);
        if (window == this.client.getWindow().getHandle() &&
                (key.getCategory().equals(InputUtil.Type.MOUSE) &&
                        key.getCode() == button) ||
                (key.getCategory().equals(InputUtil.Type.KEYSYM) &&
                        key.getCode() == InputUtil.UNKNOWN_KEY.getCode() &&
                        button == 1)) {
            if (PlayerVolumeHandler.onButton(action)) {
                ci.cancel();
                return;
            }
        }

        if (action == 1) {
            VoiceClient.mouseKeyPressed.add(button);
        } else {
            VoiceClient.mouseKeyPressed.remove(button);
        }
    }

    @Inject(at = @At("HEAD"), method = "onMouseScroll", cancellable = true)
    private void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (window == this.client.getWindow().getHandle()) {
            if (PlayerVolumeHandler.onMouseScroll(vertical)) {
                ci.cancel();
            }
        }
    }
}
