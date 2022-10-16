package su.plo.lib.mod.client.render;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.minecraft.client.renderer.ShaderInstance;

import static com.mojang.blaze3d.vertex.DefaultVertexFormat.*;

public final class ModShaders {

    public static ShaderInstance POSITION_TEX_SOLID_COLOR_SHADER;
    public static final VertexFormat POSITION_TEX_SOLID_COLOR = new VertexFormat(
            ImmutableMap.<String, VertexFormatElement>builder()
                    .put("Position",ELEMENT_POSITION)
                    .put("UV0",ELEMENT_UV0)
                    .put("Color",ELEMENT_COLOR)
                    .build()
    );

    public static ShaderInstance getPositionTexSolidColorShader() {
        return POSITION_TEX_SOLID_COLOR_SHADER;
    }

    private ModShaders() {
    }
}
