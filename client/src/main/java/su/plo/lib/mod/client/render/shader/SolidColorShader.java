package su.plo.lib.mod.client.render.shader;

import su.plo.voice.universal.shader.BlendState;
import su.plo.voice.universal.shader.UShader;
import lombok.experimental.UtilityClass;

import java.io.IOException;

@UtilityClass
public class SolidColorShader {

    private static UShader shader;

    public static void bind() {
        if (shader == null) {
            try {
                shader = ShaderUtil.loadShader(
                        "position_tex_solid_color",
                        "position_tex_solid_color",
                        BlendState.NORMAL
                );
            } catch (IOException e) {
                throw new RuntimeException("Failed to load solid color shader", e);
            }

            if (!shader.getUsable()) {
                throw new RuntimeException("Failed to load solid color shader");
            }
        }
        shader.bind();
    }

    public static void unbind() {
        if (shader != null) {
            shader.unbind();
        }
    }
}
