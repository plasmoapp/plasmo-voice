package su.plo.lib.mod.client.render.shader;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import lombok.experimental.UtilityClass;
import net.minecraft.resources.ResourceLocation;
import su.plo.lib.mod.client.ResourceLocationUtil;
import su.plo.lib.mod.client.compat.vulkan.VulkanCompat;

//#if MC>=1.21.2 && MC<1.21.5
//$$ import net.minecraft.client.renderer.ShaderDefines;
//$$ import net.minecraft.client.renderer.ShaderProgram;
//#endif

@UtilityClass
public class SolidColorShader {

    //#if MC>=1.21.5
    //$$ public static final ResourceLocation LOCATION = ResourceLocationUtil.mod("core/position_tex_solid_color");
    //#else
    public static final ResourceLocation LOCATION = ResourceLocationUtil.mod("position_tex_solid_color");
    //#endif
    public static final VertexFormat VERTEX_FORMAT = DefaultVertexFormat.POSITION_TEX_COLOR;

    //#if MC>=1.21.2 && MC<1.21.5
    //$$ public static final ShaderProgram PROGRAM = new ShaderProgram(LOCATION, VERTEX_FORMAT, ShaderDefines.EMPTY);
    //#endif

    public static boolean isAvailable() {
        if (VulkanCompat.hasVulkan()) return false;

        //#if MC>=12105
        //$$ return true;
        //#else
        return ShadersCache.isLoaded(LOCATION);
        //#endif
    }
}
