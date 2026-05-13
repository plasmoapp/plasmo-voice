//#if MC>=260200
//$$ package su.plo.voice.client.mixin.rtc;
//$$
//$$ import net.minecraft.client.network.webrtc.RtcChannel;
//$$ import net.minecraft.client.network.webrtc.RtcHandshake;
//$$ import org.spongepowered.asm.mixin.Final;
//$$ import org.spongepowered.asm.mixin.Mixin;
//$$ import org.spongepowered.asm.mixin.Shadow;
//$$ import org.spongepowered.asm.mixin.injection.At;
//$$ import org.spongepowered.asm.mixin.injection.Inject;
//$$ import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//$$ import su.plo.voice.client.rtc.RtcVoiceBridge;
//$$
//$$ @Mixin(RtcChannel.class)
//$$ public class MixinRtcChannel {
//$$
//$$     @Shadow @Final private RtcHandshake.HandshakeResult handshakeResult;
//$$
//$$     @Inject(method = "doClose", at = @At("HEAD"))
//$$     private void plasmovoice$closeVoiceBridge(CallbackInfo ci) {
//$$         RtcVoiceBridge.onPeerConnectionClosed(this.handshakeResult.peerConnection());
//$$     }
//$$ }
//#endif
