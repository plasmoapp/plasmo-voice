package su.plo.voice.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import su.plo.voice.client.VoiceClient;

@Mixin(MinecraftClient.class)
public abstract class MixinMinecraftClient {
    @Shadow
    public abstract @Nullable ServerInfo getCurrentServerEntry();

    @Inject(at = @At("HEAD"), method = "disconnect()V")
    public void onDisconnect(CallbackInfo info) {
        if (this.getCurrentServerEntry() == null) {
            return;
        }

        VoiceClient.LOGGER.info("Disconnect from " + this.getCurrentServerEntry().address);

        VoiceClient.getClientConfig().save();
        VoiceClient.disconnect();
    }
}
