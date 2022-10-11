package su.plo.lib.client.render.particle;

import org.jetbrains.annotations.NotNull;

public interface MinecraftParticles {

    @NotNull MinecraftBlockParticle createBlockParticle(int x, int y, @NotNull String blockId);
}
