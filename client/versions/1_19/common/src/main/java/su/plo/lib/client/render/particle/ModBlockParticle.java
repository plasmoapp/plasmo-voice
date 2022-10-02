package su.plo.lib.client.render.particle;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.client.render.ModVertexBuilder;
import su.plo.lib.client.render.VertexBuilder;

@RequiredArgsConstructor
public final class ModBlockParticle implements MinecraftBlockParticle {

    private final BlockDustParticle2D particle;

    @Override
    public void setGravityStrength(float gravityStrength) {
        particle.setGravityStrength(gravityStrength);
    }

    @Override
    public void setScale(float scale) {
        particle.setScale(scale);
    }

    @Override
    public void tick() {
        particle.tick();
    }

    @Override
    public void setMaxAge(int maxAge) {
        particle.setMaxAge(maxAge);
    }

    @Override
    public void setVelocity(double velocityX, double velocityY) {
        particle.setVelocity(velocityX, velocityY);
    }

    @Override
    public void buildGeometry(@NotNull VertexBuilder bufferBuilder, float delta) {
        particle.buildGeometry(((ModVertexBuilder) bufferBuilder).getBuilder(), delta);
    }

    @Override
    public boolean isAlive() {
        return particle.isAlive();
    }
}
