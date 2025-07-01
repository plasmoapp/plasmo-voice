package su.plo.lib.mod.client.render.particle;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.mod.client.render.gui.GuiRenderContext;

import java.awt.*;

public abstract class BillboardParticle2D extends Particle2D {
    protected float scale;

    protected BillboardParticle2D(double x, double y) {
        super(x, y);
        this.scale = 0.1F * (this.random.nextFloat() * 0.5F + 0.5F) * 2.0F;
    }

    protected BillboardParticle2D(double x, double y, double velocityX, double velocityY) {
        super(x, y, velocityX, velocityY);
        this.scale = 0.1F * (this.random.nextFloat() * 0.5F + 0.5F) * 2.0F;
    }

    @Override
    public void render(
            @NotNull GuiRenderContext context,
            @NotNull ResourceLocation textureLocation,
            float tickDelta
    ) {
        float newX = (float) Mth.lerp(tickDelta, this.prevPosX, this.x);
        float newY = (float) Mth.lerp(tickDelta, this.prevPosY, this.y);

        float size = this.getSize(tickDelta);

        float x0 = newX + (-1.0f * size);
        float x1 = newX + size;
        float y0 = newY + (-1.0f * size);
        float y1 = newY + size;

        float u0 = this.getMinU();
        float u1 = this.getMaxU();
        float v0 = this.getMinV();
        float v1 = this.getMaxV();

        context.blitColor(
                textureLocation,
                (int) x0,
                (int) x1,
                (int) y0,
                (int) y1,
                u0,
                u1,
                v0,
                v1,
                new Color(
                        colorRed,
                        colorGreen,
                        colorBlue,
                        colorAlpha
                )
        );
    }

    public float getSize(float tickDelta) {
        return this.scale;
    }

    public Particle2D scale(float scale) {
        this.scale *= scale;
        return super.scale(scale);
    }

    protected abstract float getMinU();

    protected abstract float getMaxU();

    protected abstract float getMinV();

    protected abstract float getMaxV();
}
