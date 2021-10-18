package su.plo.voice.client.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import su.plo.voice.client.VoiceClient;
import su.plo.voice.client.gui.PlayerVolumeHandler;

@Mixin(MouseHandler.class)
public abstract class MixinMouseHandler {

    @Shadow @Final private Minecraft minecraft;

    @Inject(at = @At("HEAD"), method = "onPress")
    private void onMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
        if (VoiceClient.getClientConfig() == null) {
            return;
        }

        if (action == 1) {
            VoiceClient.getClientConfig().keyBindings.onKeyDown(InputConstants.Type.MOUSE.getOrCreate(button));
        } else if (action == 0) {
            VoiceClient.getClientConfig().keyBindings.onKeyUp(InputConstants.Type.MOUSE.getOrCreate(button));
        }
    }

    @Inject(at = @At("HEAD"), method = "onScroll", cancellable = true)
    private void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (window == this.minecraft.getWindow().getWindow()) {
            if (PlayerVolumeHandler.onMouseScroll(vertical)) {
                ci.cancel();
            }
        }
    }
}
