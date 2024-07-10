//#if MC>=12100
//$$ package su.plo.voice.client.mixin;
//$$
//$$ import net.minecraft.client.DeltaTracker;
//$$ import net.minecraft.client.gui.Gui;
//$$ import net.minecraft.client.gui.GuiGraphics;
//$$ import org.spongepowered.asm.mixin.Mixin;
//$$ import org.spongepowered.asm.mixin.injection.At;
//$$ import org.spongepowered.asm.mixin.injection.Inject;
//$$ import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//$$ import su.plo.voice.client.ModVoiceClient;
//$$
//$$ @Mixin(Gui.class)
//$$ public class MixinGui {
//$$
//$$     @Inject(method = "render", at = @At(value = "TAIL"))
//$$     public void render(GuiGraphics drawContext, DeltaTracker tickCounter, CallbackInfo callbackInfo) {
//$$         ModVoiceClient.INSTANCE.getHudRenderer().render(drawContext, tickCounter);
//$$     }
//$$ }
//#endif