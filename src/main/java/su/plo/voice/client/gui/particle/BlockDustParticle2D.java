package su.plo.voice.client.gui.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class BlockDustParticle2D extends SpriteBillboardParticle2D {
    private final float sampleU;
    private final float sampleV;

    public BlockDustParticle2D(double x, double y, double velocityX, double velocityY, BlockState state) {
        super(x, y, velocityX, velocityY);
        Minecraft client = Minecraft.getInstance();
        this.setSprite(client.getBlockRenderer().getBlockModelShaper().getParticleIcon(state));
        this.gravityStrength = 1.0F;
        this.colorRed = 0.8F;
        this.colorGreen = 0.8F;
        this.colorBlue = 0.8F;
        if (!state.is(Blocks.GRASS_BLOCK)) {
            int i = state.getMapColor(client.level, client.player.blockPosition()).col;

            this.colorRed *= (float)(i >> 16 & 255) / 255.0F;
            this.colorGreen *= (float)(i >> 8 & 255) / 255.0F;
            this.colorBlue *= (float)(i & 255) / 255.0F;
        }

        this.sampleU = this.random.nextFloat() * 3.0F;
        this.sampleV = this.random.nextFloat() * 3.0F;
    }

    public void setGravityStrength(float gravityStrength) {
        this.gravityStrength = gravityStrength;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public ParticleRenderType getType() {
        return ParticleRenderType.TERRAIN_SHEET;
    }

    protected float getMinU() {
        return this.sprite.getU((double)((this.sampleU + 1.0F) / 4.0F * 16.0F));
    }

    protected float getMaxU() {
        return this.sprite.getU((double)(this.sampleU / 4.0F * 16.0F));
    }

    protected float getMinV() {
        return this.sprite.getV((double)(this.sampleV / 4.0F * 16.0F));
    }

    protected float getMaxV() {
        return this.sprite.getV((double)((this.sampleV + 1.0F) / 4.0F * 16.0F));
    }

    public int getBrightness(float tint) {
        return 15;
    }
}
