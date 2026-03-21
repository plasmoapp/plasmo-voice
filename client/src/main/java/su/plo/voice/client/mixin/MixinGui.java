//#if MC>=12100 && FORGE
//$$ package su.plo.voice.client.mixin;
//$$
//$$ import net.minecraft.client.DeltaTracker;
//$$ import net.minecraft.client.gui.Gui;
//$$ import net.minecraft.client.gui.GuiGraphics;
//$$ import org.spongepowered.asm.mixin.Mixin;
//$$ import org.spongepowered.asm.mixin.injection.At;
//$$ import org.spongepowered.asm.mixin.injection.Inject;
//$$ import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//$$ import su.plo.voice.client.render.ModHudRenderer;
//$$
//$$ @Mixin(Gui.class)
//$$ public class MixinGui {
//$$
//$$     @Inject(method = "render", at = @At(value = "TAIL"))
//$$     public void render(GuiGraphics drawContext, DeltaTracker tickCounter, CallbackInfo callbackInfo) {
//$$         ModHudRenderer.render(drawContext, tickCounter);
//$$     }
//$$ }
//#endif
