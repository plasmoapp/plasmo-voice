package su.plo.lib.client.render;

import org.jetbrains.annotations.NotNull;

public interface VertexBuilder {

    VertexBuilder begin(@NotNull Mode mode, @NotNull Format format);

    VertexBuilder vertex(double x, double y, double z);

    VertexBuilder uv(float u, float v);

    VertexBuilder color(int red, int green, int blue, int alpha);

    VertexBuilder color(float red, float green, float blue, float alpha);

    VertexBuilder endVertex();

    enum Mode {

        QUADS
    }

    enum Format {

        POSITION,
        POSITION_TEX,
        POSITION_COLOR,
        POSITION_TEX_COLOR,
        POSITION_TEX_SOLID_COLOR
    }
}
