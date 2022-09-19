package su.plo.voice.lib.client.render.particle;

import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public abstract class SpriteBillboardParticle2D extends BillboardParticle2D {
    protected TextureAtlasSprite sprite;

    protected SpriteBillboardParticle2D(double x, double y) {
        super(x, y);
    }

    protected SpriteBillboardParticle2D(double x, double y, double velocityX, double velocityY) {
        super(x, y, velocityX, velocityY);
    }

    protected void setSprite(TextureAtlasSprite sprite) {
        this.sprite = sprite;
    }

    protected float getMinU() {
        return this.sprite.getU0();
    }

    protected float getMaxU() {
        return this.sprite.getU1();
    }

    protected float getMinV() {
        return this.sprite.getV0();
    }

    protected float getMaxV() {
        return this.sprite.getV1();
    }

    public void setSprite(SpriteSet spriteProvider) {
        this.setSprite(spriteProvider.get(this.random));
    }

    public void setSpriteForAge(SpriteSet spriteProvider) {
        if (!this.dead) {
            this.setSprite(spriteProvider.get(this.age, this.maxAge));
        }
    }
}
