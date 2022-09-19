package su.plo.voice.lib.client.render.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.core.particles.ParticleGroup;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.AABB;

import java.util.Optional;

public abstract class Particle2D {
    private static final AABB EMPTY_BOUNDING_BOX = new AABB(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D);
    protected double prevPosX;
    protected double prevPosY;
    protected double x;
    protected double y;
    protected double velocityX;
    protected double velocityY;
    private AABB boundingBox;
    protected boolean onGround;
    protected boolean collidesWithWorld;
    private boolean field_21507;
    protected boolean dead;
    protected float spacingXZ;
    protected float spacingY;
    protected final RandomSource random;
    protected int age;
    protected int maxAge;
    protected float gravityStrength;
    protected float colorRed;
    protected float colorGreen;
    protected float colorBlue;
    protected float colorAlpha;
    protected float field_28786;
    protected boolean field_28787;

    protected Particle2D(double x, double y) {
        this.boundingBox = EMPTY_BOUNDING_BOX;
        this.collidesWithWorld = true;
        this.spacingXZ = 0.6F;
        this.spacingY = 1.8F;
        this.random = RandomSource.create();
        this.colorRed = 1.0F;
        this.colorGreen = 1.0F;
        this.colorBlue = 1.0F;
        this.colorAlpha = 1.0F;
        this.field_28786 = 0.98F;
        this.field_28787 = false;
        this.setBoundingBoxSpacing(0.2F, 0.2F);
        this.setPos(x, y);
        this.prevPosX = x;
        this.prevPosY = y;
        this.maxAge = (int) (4.0F / (this.random.nextFloat() * 0.9F + 0.1F));
    }

    public Particle2D(double x, double y, double velocityX, double velocityY) {
        this(x, y);
        this.velocityX = velocityX + (Math.random() * 2.0D - 1.0D) * 0.4000000059604645D;
        this.velocityY = velocityY + (Math.random() * 2.0D - 1.0D) * 0.4000000059604645D;
        double d = (Math.random() + Math.random() + 1.0D) * 0.15000000596046448D;
        double e = Math.sqrt(this.velocityX * this.velocityX + this.velocityY * this.velocityY);
        this.velocityX = this.velocityX / e * d * 0.4000000059604645D;
        this.velocityY = this.velocityY / e * d * 0.4000000059604645D + 0.10000000149011612D;
    }

    public Particle2D move(float speed) {
        this.velocityX *= (double) speed;
        this.velocityY = (this.velocityY - 0.10000000149011612D) * (double) speed + 0.10000000149011612D;
        return this;
    }

    public void setVelocity(double velocityX, double velocityY) {
        this.velocityX = velocityX;
        this.velocityY = velocityY;
    }

    public Particle2D scale(float scale) {
        this.setBoundingBoxSpacing(0.2F * scale, 0.2F * scale);
        return this;
    }

    public void setColor(float red, float green, float blue) {
        this.colorRed = red;
        this.colorGreen = green;
        this.colorBlue = blue;
    }

    protected void setColorAlpha(float alpha) {
        this.colorAlpha = alpha;
    }

    public void setMaxAge(int maxAge) {
        this.maxAge = maxAge;
    }

    public int getMaxAge() {
        return this.maxAge;
    }

    public void tick() {
        this.prevPosX = this.x;
        this.prevPosY = this.y;
        if (this.age++ >= this.maxAge) {
            this.markDead();
        } else {
            this.velocityY += 0.04D * (double) this.gravityStrength;
            this.move(this.velocityX, this.velocityY);
            if (this.field_28787 && this.y == this.prevPosY) {
                this.velocityX *= 1.1D;
            }

            this.velocityX *= (double) this.field_28786;
            this.velocityY *= (double) this.field_28786;
            if (this.onGround) {
                this.velocityX *= 0.699999988079071D;
            }
        }
    }

    public abstract void buildGeometry(VertexConsumer vertexConsumer, float tickDelta);

    public abstract ParticleRenderType getType();

    public String toString() {
        String var10000 = this.getClass().getSimpleName();
        return var10000 + ", Pos (" + this.x + "," + this.y + "), RGBA (" + this.colorRed + "," + this.colorGreen + "," + this.colorBlue + "," + this.colorAlpha + "), Age " + this.age;
    }

    public void markDead() {
        this.dead = true;
    }

    protected void setBoundingBoxSpacing(float spacingXZ, float spacingY) {
        if (spacingXZ != this.spacingXZ || spacingY != this.spacingY) {
            this.spacingXZ = spacingXZ;
            this.spacingY = spacingY;
            AABB box = this.getBoundingBox();
            double d = (box.minX + box.maxX - (double) spacingXZ) / 2.0D;
            double e = (box.minZ + box.maxZ - (double) spacingXZ) / 2.0D;
            this.setBoundingBox(new AABB(d, box.minY, e, d + (double) this.spacingXZ, box.minY + (double) this.spacingY, e + (double) this.spacingXZ));
        }

    }

    public void setPos(double x, double y) {
        this.x = x;
        this.y = y;
        float f = this.spacingXZ / 2.0F;
        float g = this.spacingY;
        this.setBoundingBox(new AABB(x - (double) f, y, 0 - (double) f, x + (double) f, y + (double) g, 0 + (double) f));
    }

    public void move(double dx, double dy) {
        if (!this.field_21507) {
            if (dx != 0.0D || dy != 0.0D) {
                this.setBoundingBox(this.getBoundingBox().move(dx, dy, 0));
                this.repositionFromBoundingBox();
            }

            if (Math.abs(dy) >= 9.999999747378752E-6D && Math.abs(dy) < 9.999999747378752E-6D) {
                this.field_21507 = true;
            }

        }
    }

    protected void repositionFromBoundingBox() {
        AABB box = this.getBoundingBox();
        this.x = (box.minX + box.maxX) / 2.0D;
        this.y = box.minY;
    }

    protected int getBrightness(float tint) {
        return 15;
    }

    public boolean isAlive() {
        return !this.dead;
    }

    public AABB getBoundingBox() {
        return this.boundingBox;
    }

    public void setBoundingBox(AABB boundingBox) {
        this.boundingBox = boundingBox;
    }

    public Optional<ParticleGroup> getGroup() {
        return Optional.empty();
    }
}
