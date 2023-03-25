package su.plo.voice.client.mixin;

//#if FORGE
//$$ import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
//$$ import net.minecraft.resources.ResourceLocation;
//$$ import net.minecraft.server.level.ServerPlayer;
//$$ import net.minecraft.server.network.ServerGamePacketListenerImpl;
//$$ import org.spongepowered.asm.mixin.Mixin;
//$$ import org.spongepowered.asm.mixin.Shadow;
//$$ import org.spongepowered.asm.mixin.injection.At;
//$$ import org.spongepowered.asm.mixin.injection.Inject;
//$$ import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//$$ import su.plo.voice.server.connection.ModServerChannelHandler;
//$$
//$$ @Mixin(ServerGamePacketListenerImpl.class)
//$$ public abstract class MixinServerGamePacketListenerImpl {
//$$
//$$     @Shadow
//$$     public abstract ServerPlayer getPlayer();
//$$
//$$     private static final ResourceLocation REGISTER = new ResourceLocation("minecraft:register");
//$$
//$$     @Inject(method = "handleCustomPayload", at = @At("HEAD"))
//$$     public void handleCustomPayload(ServerboundCustomPayloadPacket packet, CallbackInfo ci) {
//$$         if (packet.getIdentifier().equals(REGISTER) && ModServerChannelHandler.INSTANCE != null) {
//$$             ModServerChannelHandler.INSTANCE.onChannelRegister(getPlayer(), packet);
//$$         }
//$$     }
//$$ }
//#endif
