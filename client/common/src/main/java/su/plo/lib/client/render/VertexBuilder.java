package su.plo.lib.client.render;

import org.jetbrains.annotations.NotNull;

public interface VertexBuilder {

    VertexBuilder begin(@NotNull Mode mode, @NotNull Format format);

    VertexBuilder vertex(double x, double y, double z);

    VertexBuilder vertex(@NotNull MinecraftMatrix matrix, double x, double y, double z);

    VertexBuilder uv(float u, float v);

    VertexBuilder overlayCoords(int i, int j);

    VertexBuilder uv2(int i, int j);

    VertexBuilder normal(float f, float g, float h);

    VertexBuilder normal(@NotNull MinecraftMatrix matrix, float f, float g, float h);

    default VertexBuilder uv2(int i) {
        return uv2(i & '\uffff', i >> 16 & '\uffff');
    }

    default VertexBuilder overlayCoords(int i) {
        return overlayCoords(i & '\uffff', i >> 16 & '\uffff');
    }


    VertexBuilder color(int red, int green, int blue, int alpha);

    VertexBuilder color(float red, float green, float blue, float alpha);

    VertexBuilder endVertex();

    enum Mode {

        QUADS
    }

    enum Shader {

        POSITION,
        POSITION_TEX,
        POSITION_COLOR,
        POSITION_TEX_COLOR,
        POSITION_TEX_SOLID_COLOR,
        POSITION_COLOR_TEX_LIGHTMAP,
        RENDERTYPE_TEXT,
        RENDERTYPE_TEXT_SEE_THROUGH
    }

    enum Format {

        POSITION,
        POSITION_TEX,
        POSITION_COLOR,
        POSITION_TEX_COLOR,
        POSITION_TEX_SOLID_COLOR,
        POSITION_COLOR_TEX_LIGHTMAP
    }
}
