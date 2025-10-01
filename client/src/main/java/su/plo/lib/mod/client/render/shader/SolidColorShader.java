package su.plo.lib.mod.client.render.shader;

import lombok.experimental.UtilityClass;
import su.plo.lib.mod.client.compat.vulkan.VulkanCompat;

//#if MC<12105
import gg.essential.universal.shader.BlendState;
import gg.essential.universal.shader.UShader;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
//#endif

@UtilityClass
public class SolidColorShader {

    public static boolean isAvailable() {
        //#if MC>=12105
        //$$ return !VulkanCompat.hasVulkan();
        //#else
        return getShader() != null;
        //#endif
    }

    //#if MC<12105
    private static UShader shader;

    public static @Nullable UShader getShader() {
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
    }
    //#endif
}
