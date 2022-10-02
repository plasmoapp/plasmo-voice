package su.plo.lib.client.render;

import org.jetbrains.annotations.NotNull;

// Tesselator
public interface MinecraftTesselator {

    @NotNull VertexBuilder getBuilder();

    void end();
}
