package su.plo.lib.mod.client.render;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.lib.api.client.render.MinecraftMatrix;
import su.plo.lib.api.client.render.VertexBuilder;

@RequiredArgsConstructor
public final class ModVertexBuilder implements VertexBuilder {

    public static @Nullable VertexFormat getFormat(@NotNull Format format) {
        return switch (format) {
            case POSITION -> DefaultVertexFormat.POSITION;
            case POSITION_COLOR -> DefaultVertexFormat.POSITION_COLOR;
            case POSITION_COLOR_LIGHTMAP -> DefaultVertexFormat.POSITION_COLOR_LIGHTMAP;
            case POSITION_TEX -> DefaultVertexFormat.POSITION_TEX;
            case POSITION_TEX_COLOR -> DefaultVertexFormat.POSITION_TEX_COLOR;
            case POSITION_TEX_SOLID_COLOR -> ModShaders.POSITION_TEX_SOLID_COLOR;
            case POSITION_COLOR_TEX_LIGHTMAP -> DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP;
            default -> null;
        };
    }

    @Getter
    private final BufferBuilder builder;

    @Override
    public VertexBuilder begin(@NotNull Mode mode, @NotNull Format format) {
        VertexFormat.Mode mcMode = getMode(mode);
        VertexFormat mcFormat = getFormat(format);
        if (mcMode == null || mcFormat == null) return this;

        builder.begin(mcMode, mcFormat);
        return this;
    }

    @Override
    public VertexBuilder vertex(double x, double y, double z) {
        builder.vertex(x, y, z);
        return this;
    }

    @Override
    public VertexBuilder vertex(@NotNull MinecraftMatrix matrix, double x, double y, double z) {
        builder.vertex(((ModMatrix) matrix).getPoseStack().last().pose(), (float) x, (float) y, (float) z);
        return this;
    }

    @Override
    public VertexBuilder uv(float u, float v) {
        builder.uv(u, v);
        return this;
    }

    @Override
    public VertexBuilder overlayCoords(int i, int j) {
        builder.overlayCoords(i, j);
        return this;
    }

    @Override
    public VertexBuilder uv2(int i, int j) {
        builder.uv2(i, j);
        return this;
    }

    @Override
    public VertexBuilder normal(float f, float g, float h) {
        builder.normal(f, g, h);
        return this;
    }

    @Override
    public VertexBuilder normal(@NotNull MinecraftMatrix matrix, float f, float g, float h) {
        builder.normal(((ModMatrix) matrix).getPoseStack().last().normal(), f, g, h);
        return this;
    }

    @Override
    public VertexBuilder color(int red, int green, int blue, int alpha) {
        builder.color(red, green, blue, alpha);
        return this;
    }

    @Override
    public VertexBuilder color(float red, float green, float blue, float alpha) {
        builder.color(red, green, blue, alpha);
        return this;
    }

    @Override
    public VertexBuilder endVertex() {
        builder.endVertex();
        return this;
    }

    private VertexFormat.Mode getMode(@NotNull Mode mode) {
        return switch (mode) {
            case LINES -> VertexFormat.Mode.LINES;
            case LINE_STRIP -> VertexFormat.Mode.LINE_STRIP;
            case DEBUG_LINES -> VertexFormat.Mode.DEBUG_LINES;
            case DEBUG_LINE_STRIP -> VertexFormat.Mode.DEBUG_LINE_STRIP;
            case TRIANGLES -> VertexFormat.Mode.TRIANGLES;
            case TRIANGLE_STRIP -> VertexFormat.Mode.TRIANGLE_STRIP;
            case TRIANGLE_FAN -> VertexFormat.Mode.TRIANGLE_FAN;
            case QUADS -> VertexFormat.Mode.QUADS;
        };
    }
}
