package su.plo.voice.mod.mixin;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import su.plo.lib.mod.client.render.ModShaders;

import java.io.IOException;
import java.util.Map;

@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer {

    @Shadow @Final private Map<String, ShaderInstance> shaders;

    @Inject(method = "reloadShaders", at = @At("RETURN"))
    public void reloadShaders(ResourceManager resourceManager, CallbackInfo ci) {
        try {
            ModShaders.POSITION_TEX_SOLID_COLOR_SHADER = new ShaderInstance(
                    resourceManager,
                    "position_tex_solid_color",
                    ModShaders.POSITION_TEX_SOLID_COLOR
            );

            shaders.put(
                    ModShaders.POSITION_TEX_SOLID_COLOR_SHADER.getName(),
                    ModShaders.POSITION_TEX_SOLID_COLOR_SHADER
            );
        } catch (IOException e) {
            throw new RuntimeException("could not reload shaders", e);
        }
    }
}
