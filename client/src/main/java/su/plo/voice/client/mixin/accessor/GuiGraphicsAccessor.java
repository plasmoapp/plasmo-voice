package su.plo.voice.client.mixin.accessor;

//#if MC>=12106
//$$ import net.minecraft.client.gui.GuiGraphics;
//$$ import net.minecraft.resources.ResourceLocation;
//$$ import org.spongepowered.asm.mixin.Mixin;
//$$ import org.spongepowered.asm.mixin.gen.Invoker;
//$$
//#if MC>=26.4
//$$ import net.minecraft.client.gui.GuiPipeline;
//#else
//$$ import com.mojang.blaze3d.pipeline.RenderPipeline;
//#endif
//$$
//$$ @Mixin(GuiGraphics.class)
//$$ public interface GuiGraphicsAccessor {
//$$     @Invoker("innerBlit")
    //#if MC>=26.4
    //$$ void plasmovoice_innerBlit(GuiPipeline pipeline, Identifier textureLocation, int x0, int x1, int y0, int y1, float u0, float u1, float v0, float v1, int color);
    //#else
    //$$ void plasmovoice_innerBlit(RenderPipeline pipeline, ResourceLocation textureLocation, int x0, int x1, int y0, int y1, float u0, float u1, float v0, float v1, int color);
    //#endif
//$$ }
//#endif
