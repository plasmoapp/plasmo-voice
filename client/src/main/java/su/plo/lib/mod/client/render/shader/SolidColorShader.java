package su.plo.lib.mod.client.render.shader;

import lombok.experimental.UtilityClass;
import net.minecraft.resources.ResourceLocation;
import su.plo.lib.mod.client.ResourceLocationUtil;
import su.plo.lib.mod.client.compat.vulkan.VulkanCompat;

//#if MC<12105
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
//#endif

@UtilityClass
public class SolidColorShader {

    public static final ResourceLocation LOCATION =
            //#if MC>=12106
            //$$ ResourceLocationUtil.mod("position_tex_solid_color_1_21_6");
            //#else
            ResourceLocationUtil.mod("position_tex_solid_color");
            //#endif

    public static boolean isAvailable() {
        if (VulkanCompat.hasVulkan()) return false;

        //#if MC>=12105
        //$$ return true;
        //#else
        return ShadersCache.isUsable(LOCATION, DefaultVertexFormat.POSITION_TEX_COLOR);
        //#endif
    }
}
