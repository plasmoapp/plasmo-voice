package su.plo.lib.client.render;

import com.mojang.blaze3d.vertex.Tesselator;
import org.jetbrains.annotations.NotNull;

public final class ModTesselator implements MinecraftTesselator {

    private VertexBuilder builder;

    @Override
    public @NotNull VertexBuilder getBuilder() {
        if (builder == null) {
            this.builder = new ModVertexBuilder(Tesselator.getInstance().getBuilder());
        }

        return builder;
    }

    @Override
    public void end() {
        Tesselator.getInstance().end();
    }
}
