package su.plo.lib.mod.client.render.shader;

import gg.essential.universal.shader.BlendState;
import gg.essential.universal.shader.UShader;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Nullable;
import su.plo.lib.mod.client.compat.vulkan.VulkanCompat;

import java.io.IOException;

@UtilityClass
public class SolidColorShader {

    private static UShader shader;

    public static boolean isAvailable() {
        //#if MC>=12105
        //$$ return !VulkanCompat.hasVulkan();
        //#else
        return getShader() != null;
        //#endif
    }

    public static @Nullable UShader getShader() {
        //#if MC>=12105
        //$$ return null;
        //#else
        if (VulkanCompat.hasVulkan()) return null;

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
}
