package su.plo.voice.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import su.plo.voice.client.VoiceClient;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft {
    @Shadow @Nullable public abstract ServerData getCurrentServer();

    @Inject(at = @At("HEAD"), method = "clearLevel()V")
    public void onDisconnect(CallbackInfo info) {
        if (this.getCurrentServer() == null) {
            return;
        }

        VoiceClient.LOGGER.info("Disconnect from " + this.getCurrentServer().ip);

        VoiceClient.getClientConfig().save();
        VoiceClient.disconnect();
        VoiceClient.socketUDP = null;
    }
}
