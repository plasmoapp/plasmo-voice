package su.plo.voice.client.mixin;

//#if MC>=12109
//$$ import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
//$$ import com.mojang.blaze3d.buffers.GpuBufferSlice;
//$$ import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
//$$ import com.mojang.blaze3d.resource.ResourceHandle;
//$$ import com.mojang.blaze3d.vertex.PoseStack;
//$$ import net.minecraft.client.Camera;
//$$ import net.minecraft.client.DeltaTracker;
//$$ import net.minecraft.client.multiplayer.ClientLevel;
//$$ import net.minecraft.client.renderer.LevelRenderer;
//$$ import net.minecraft.client.renderer.culling.Frustum;
//$$ import net.minecraft.client.renderer.state.LevelRenderState;
//$$ import net.minecraft.util.profiling.ProfilerFiller;
//$$ import org.joml.Matrix4f;
//$$ import org.joml.Vector4f;
//$$ import org.spongepowered.asm.mixin.Mixin;
//$$ import org.spongepowered.asm.mixin.Shadow;
//$$ import org.spongepowered.asm.mixin.Unique;
//$$ import org.spongepowered.asm.mixin.injection.At;
//$$ import org.spongepowered.asm.mixin.injection.Inject;
//$$ import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//$$ import su.plo.lib.mod.client.render.world.LevelRendererContext;
//$$ import su.plo.voice.client.ModVoiceClient;
//$$
//$$ @Mixin(LevelRenderer.class)
//$$ public class MixinLevelRenderer {
//$$
//$$     @Shadow
//$$     private ClientLevel level;
//$$
//$$     @Unique
//$$     private final LevelRendererContext context = new LevelRendererContext();
//$$
//$$     @Inject(method = "renderLevel", at = @At("HEAD"))
//$$     private void renderLevel(
//$$             GraphicsResourceAllocator resourceAllocator,
//$$             DeltaTracker deltaTracker,
//$$             boolean renderBlockOutline,
//$$             Camera camera,
//$$             Matrix4f matrix4f,
//$$             Matrix4f matrix4f2,
//$$             Matrix4f matrix4f3,
//$$             GpuBufferSlice gpuBufferSlice,
//$$             Vector4f vector4f,
//$$             boolean bl2,
//$$             CallbackInfo ci
//$$     ) {
//$$         context.update(level, deltaTracker);
//$$     }
//$$
//$$     @ModifyExpressionValue(
//$$             method = "method_62214",
//$$             at = @At(value = "NEW", target = "com/mojang/blaze3d/vertex/PoseStack")
//$$     )
//$$     private PoseStack setPoseStack(PoseStack matrixStack) {
//$$         context.setStack(matrixStack);
//$$         return matrixStack;
//$$     }
//$$
//$$     @Inject(method = "method_62214", at = @At("RETURN"))
//$$     private void afterRender(
//$$             GpuBufferSlice gpuBufferSlice,
//$$             LevelRenderState levelRenderState,
//$$             ProfilerFiller profilerFiller,
//$$             Matrix4f matrix4f,
//$$             ResourceHandle<?> resourceHandle,
//$$             ResourceHandle<?> resourceHandle2,
//$$             boolean bl,
//#if MC<12111
//$$             Frustum frustum,
//#endif
//$$             ResourceHandle<?> resourceHandle3,
//$$             ResourceHandle<?> resourceHandle4,
//$$             CallbackInfo ci
//$$     ) {
//$$         ModVoiceClient.INSTANCE.getLevelRenderer().render(
//$$                 context.getLevel(),
//$$                 context.getStack(),
//$$                 context.getDeltaTracker().getRealtimeDeltaTicks()
//$$         );
//$$     }
//$$ }
//#endif
