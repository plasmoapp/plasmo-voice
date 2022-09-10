package su.plo.voice.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import su.plo.voice.api.client.config.keybind.KeyBinding;
import su.plo.voice.client.ModVoiceClient;
import su.plo.voice.client.event.key.KeyPressedEvent;
import su.plo.voice.client.event.key.MouseScrollEvent;

@Mixin(MouseHandler.class)
public abstract class MixinMouseHandler {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(at = @At("HEAD"), method = "onPress")
    private void onMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
        if (window != this.minecraft.getWindow().getWindow()) return;

        KeyPressedEvent event = new KeyPressedEvent(
                KeyBinding.Type.MOUSE.getOrCreate(button),
                KeyBinding.Action.fromInt(action)
        );

        ModVoiceClient.INSTANCE.getEventBus().call(event);
    }

    @Inject(at = @At("HEAD"), method = "onScroll", cancellable = true)
    private void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        MouseScrollEvent event = new MouseScrollEvent(horizontal, vertical);
        if (event.isCancelled()) ci.cancel();
    }
}
