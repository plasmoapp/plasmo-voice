package su.plo.lib.api.client.render.particle;

import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.client.render.VertexBuilder;

public interface MinecraftParticle {

    void tick();

    void setMaxAge(int maxAge);

    void setVelocity(double velocityX, double velocityY);

    void buildGeometry(@NotNull VertexBuilder bufferBuilder, float delta);

    boolean isAlive();
}
