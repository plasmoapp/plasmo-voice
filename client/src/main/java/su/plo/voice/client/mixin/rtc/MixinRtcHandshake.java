//#if MC>=260200
//$$ package su.plo.voice.client.mixin.rtc;
//$$
//$$ import dev.onvoid.webrtc.RTCDataChannel;
//$$ import dev.onvoid.webrtc.RTCPeerConnection;
//$$ import net.minecraft.client.network.webrtc.RtcHandshake;
//$$ import org.spongepowered.asm.mixin.Final;
//$$ import org.spongepowered.asm.mixin.Mixin;
//$$ import org.spongepowered.asm.mixin.Shadow;
//$$ import org.spongepowered.asm.mixin.injection.At;
//$$ import org.spongepowered.asm.mixin.injection.Inject;
//$$ import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//$$ import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//$$ import su.plo.voice.client.rtc.RtcVoiceBridge;
//$$
//$$ import java.util.concurrent.CompletableFuture;
//$$
//$$ @Mixin(RtcHandshake.class)
//$$ public class MixinRtcHandshake {
//$$
//$$     @Shadow @Final private RTCPeerConnection peerConnection;
//$$
//$$     @Inject(
//$$         method = "createOffer",
//$$         at = @At(
//$$             value = "INVOKE",
//$$             target = "Lnet/minecraft/client/network/webrtc/RtcHandshake;wireDataChannel(Ldev/onvoid/webrtc/RTCDataChannel;)V",
//$$             shift = At.Shift.AFTER
//$$         )
//$$     )
//$$     private void plasmovoice$createVoiceDataChannel(CallbackInfoReturnable<CompletableFuture<String>> cir) {
//$$         RtcVoiceBridge.openVoiceChannel(this.peerConnection);
//$$     }
//$$
//$$     @Inject(method = "wireDataChannel", at = @At("HEAD"), cancellable = true)
//$$     private void plasmovoice$interceptVoiceDataChannel(RTCDataChannel dataChannel, CallbackInfo ci) {
//$$         if (RtcVoiceBridge.VOICE_DATA_CHANNEL_LABEL.equals(dataChannel.getLabel())) {
//$$             RtcVoiceBridge.acceptVoiceChannel(this.peerConnection, dataChannel);
//$$             ci.cancel();
//$$         }
//$$     }
//$$ }
//#endif
