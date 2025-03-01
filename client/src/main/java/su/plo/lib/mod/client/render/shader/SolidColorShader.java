package su.plo.lib.mod.client.render.shader;

import gg.essential.universal.shader.BlendState;
import gg.essential.universal.shader.UShader;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.BaseVoice;

import java.io.IOException;

@UtilityClass
public class SolidColorShader {

    private static UShader shader;

    public static boolean isAvailable() {
        //#if MC>=12105
        //$$ return !hasVulkan();
        //#else
        return getShader() != null;
        //#endif
    }

    public static @Nullable UShader getShader() {
        //#if MC>=12105
        //$$ return null;
        //#else
        if (hasVulkan()) return null;

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
                //$$         "position_tex_solid_color_1_16",
                //$$         "position_tex_solid_color_1_16",
                //$$         BlendState.NORMAL
                //$$ );
                //#endif
            } catch (IOException e) {
                throw new RuntimeException("Failed to load solid color shader", e);
            }

            if (!shader.getUsable()) {
                throw new RuntimeException("Failed to load solid color shader");
            }
        }

        return shader;
        //#endif
    }

    private static boolean hasVulkan = false;

    private static boolean hasVulkan() {
        if (hasVulkan) return true;

        try {
            Class.forName("net.vulkanmod.vulkan.Vulkan");
            BaseVoice.LOGGER.warn("Shaders are not supported for Vulkan yet");
            hasVulkan = true;
            return true;
        } catch (ClassNotFoundException ignored) {
        }

        return false;
    }
}
