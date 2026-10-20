package su.plo.voice.client.mixin.accessor;

//#if MC>=12111
//$$ import net.minecraft.client.renderer.rendertype.RenderSetup;
//$$ import net.minecraft.client.renderer.rendertype.RenderType;
//$$ import org.spongepowered.asm.mixin.Mixin;
//$$ import org.spongepowered.asm.mixin.gen.Invoker;
//$$
//#if MC>=26.4
//$$ import com.mojang.blaze3d.pipeline.RenderPipeline;
//$$ import org.spongepowered.asm.mixin.gen.Accessor;
//#endif
//$$
//$$ @Mixin(RenderType.class)
//$$ public interface RenderTypeAccessor {
//$$     @Invoker("create")
//$$     static RenderType plasmovoice_create(String name, RenderSetup setup) {
//$$         throw new AssertionError();
//$$     }
    //#if MC>=26.4
    //$$
    //$$ @Accessor("nonOitPipeline")
    //$$ RenderPipeline plasmovoice_getNonOitPipeline();
    //#endif
//$$ }
//#endif
