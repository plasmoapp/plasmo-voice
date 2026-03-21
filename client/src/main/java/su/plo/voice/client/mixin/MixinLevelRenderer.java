package su.plo.voice.client.mixin;

//#if MC>=12109
//$$ import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
//$$ import com.mojang.blaze3d.buffers.GpuBufferSlice;
//$$ import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
//$$ import com.mojang.blaze3d.resource.ResourceHandle;
//$$ import com.mojang.blaze3d.vertex.PoseStack;
//$$ import net.minecraft.client.DeltaTracker;
//$$ import net.minecraft.client.multiplayer.ClientLevel;
//$$ import net.minecraft.client.renderer.LevelRenderer;
//$$ import net.minecraft.client.renderer.culling.Frustum;
//$$ import net.minecraft.client.renderer.state.LevelRenderState;
//$$ import net.minecraft.util.profiling.ProfilerFiller;
//$$ import org.joml.Vector4f;
//$$ import org.spongepowered.asm.mixin.Mixin;
//$$ import org.spongepowered.asm.mixin.Shadow;
//$$ import org.spongepowered.asm.mixin.Unique;
//$$ import org.spongepowered.asm.mixin.injection.At;
//$$ import org.spongepowered.asm.mixin.injection.Inject;
//$$ import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//$$ import su.plo.lib.mod.client.render.world.LevelRendererContext;
//$$ import su.plo.voice.client.render.ModLevelRenderer;
//$$
//#if MC>=26.1
//$$ import net.minecraft.client.renderer.chunk.ChunkSectionsToRender;
//$$ import net.minecraft.client.renderer.state.level.CameraRenderState;
//$$ import org.joml.Matrix4fc;
//#else
//$$ import net.minecraft.client.Camera;
//$$ import org.joml.Matrix4f;
//#endif
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
//#if MC>=26.1
//$$     @Inject(method = "renderLevel", at = @At("HEAD"))
//$$     private void renderLevel(
//$$             GraphicsResourceAllocator resourceAllocator,
//$$             DeltaTracker deltaTracker,
//$$             boolean renderOutline,
//$$             CameraRenderState cameraState,
//$$             Matrix4fc modelViewMatrix,
//$$             GpuBufferSlice terrainFog,
//$$             Vector4f fogColor,
//$$             boolean shouldRenderSky,
//$$             ChunkSectionsToRender chunkSectionsToRender,
//$$             CallbackInfo ci
//$$     ) {
//$$         context.update(level, deltaTracker);
//$$     }
//#else
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
//#endif
//$$
//$$     @ModifyExpressionValue(
//#if MC>=26.1
//$$             method = "lambda$addMainPass$0",
//#else
//$$             method = "method_62214",
//#endif
//$$             at = @At(value = "NEW", target = "com/mojang/blaze3d/vertex/PoseStack")
//$$     )
//$$     private PoseStack setPoseStack(PoseStack matrixStack) {
//$$         context.setStack(matrixStack);
//$$         return matrixStack;
//$$     }
//$$
//#if MC>=26.1
//$$     @Inject(method = "lambda$addMainPass$0", at = @At("RETURN"))
//$$     private void afterRender(CallbackInfo ci) {
//$$         ModLevelRenderer.render(
//$$                 context.getLevel(),
//$$                 context.getStack(),
//$$                 context.getDeltaTracker().getRealtimeDeltaTicks()
//$$         );
//$$     }
//#else
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
//$$         ModLevelRenderer.render(
//$$                 context.getLevel(),
//$$                 context.getStack(),
//$$                 context.getDeltaTracker().getRealtimeDeltaTicks()
//$$         );
//$$     }
//#endif
//$$ }
//#endif
