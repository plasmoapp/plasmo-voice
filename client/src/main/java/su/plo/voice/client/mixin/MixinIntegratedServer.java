package su.plo.voice.client.mixin;

import net.minecraft.client.server.IntegratedServer;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import su.plo.voice.server.ModVoiceServer;

@Mixin(IntegratedServer.class)
public class MixinIntegratedServer {

    @Inject(method = "publishServer", at = @At("RETURN"))
    private void publishServer(GameType gameType, boolean allowCheats, int publishPort, CallbackInfoReturnable<Boolean> cir) {
        boolean published = cir.getReturnValue();
        if (!published) return;
        if (ModVoiceServer.INSTANCE.getConfig().host().port() != 0) return;

        ModVoiceServer.INSTANCE.startUdpServer();
    }
}
