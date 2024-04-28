package su.plo.lib.mod.client.render.shader;

import gg.essential.universal.shader.BlendState;
import gg.essential.universal.shader.SamplerUniform;
import gg.essential.universal.shader.UShader;
import lombok.experimental.UtilityClass;

import java.io.IOException;

@UtilityClass
public class SolidColorShader {

    private static UShader shader;
    private static SamplerUniform samplerUniform;

    public static void bind(int textureId) {
        if (shader == null) {
            try {
                //#if MC>=11701
                shader = ShaderUtil.loadShader(
                        "position_tex_solid_color",
                        "position_tex_solid_color",
                        BlendState.NORMAL
                );
                //#else
                //$$ shader = ShaderUtil.loadShader(
                //$$         "position_tex_solid_color_legacy",
                //$$         "position_tex_solid_color_legacy",
                //$$         BlendState.NORMAL
                //$$ );
                //#endif
            } catch (IOException e) {
                throw new RuntimeException("Failed to load solid color shader", e);
            }

            if (!shader.getUsable()) {
                throw new RuntimeException("Failed to load solid color shader");
            }

            samplerUniform = shader.getSamplerUniform("TextureSampler");
        }

        shader.bind();

        samplerUniform.setValue(textureId);
    }

    public static void unbind() {
        if (shader != null) {
            shader.unbind();
        }
    }
}
