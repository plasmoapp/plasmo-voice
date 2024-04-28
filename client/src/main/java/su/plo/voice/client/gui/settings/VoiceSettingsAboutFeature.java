package su.plo.voice.client.gui.settings;

import com.google.common.collect.Lists;
import gg.essential.universal.UGraphics;
import gg.essential.universal.UMatrixStack;
import gg.essential.universal.USound;
import lombok.RequiredArgsConstructor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.mod.client.render.RenderUtil;
import su.plo.lib.mod.client.render.particle.BlockDustParticle2D;
import su.plo.voice.util.RandomUtil;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public final class VoiceSettingsAboutFeature {

    private final VoiceSettingsScreen parent;

    private final List<BlockDustParticle2D> particles = Lists.newArrayList();

    private int clickCount;
    private long lastClick;

    public void tick() {
        particles.removeAll(
                particles.stream()
                        .filter((particle) -> {
                            particle.tick();
                            return !particle.isAlive();
                        }).collect(Collectors.toList())
        );

        if (System.currentTimeMillis() - lastClick > 5000L) {
            this.clickCount = 0;
        }
    }

    public void titleClicked() {
        if (clickCount > 10) {
            parent.getNavigation().openTab(-1);
            this.lastClick = 0L;
            this.clickCount = 0;

            USound.INSTANCE.playButtonPress();
            return;
        }

        this.lastClick = System.currentTimeMillis();
        this.clickCount++;

        USound.INSTANCE.playSoundStatic(
                SoundEvents.GRAVEL_HIT,
                1F,
                1F
        );

        for (int i = 0; i < 2 + RandomUtil.randomInt(3); i++) {
            BlockDustParticle2D particle = new BlockDustParticle2D(
                    14 + RandomUtil.randomInt(parent.getTitleWidth()),
                    15 + RandomUtil.randomInt(UGraphics.getFontHeight()),
                    0D,
                    0D,
                    Blocks.DIRT.defaultBlockState()
            );
            particle.setMaxAge(10 + RandomUtil.randomInt(25));
            particle.setVelocity(RandomUtil.randomFloat(-0.25f, 0.25f), -0.25f);
            particle.setGravityStrength(4.0f);
            particle.setScale(RandomUtil.randomFloat(1.5f, 2.5f));
            particles.add(particle);
        }
    }

    public void render(@NotNull UMatrixStack stack, float delta) {
        UGraphics buffer = UGraphics.getFromTessellator();

        for (BlockDustParticle2D particle : particles) {
//            render.setShader(VertexBuilder.Shader.POSITION_TEX_COLOR);
            UGraphics.color4f(1.0F, 1.0F, 1.0F, 1.0F);

            UGraphics.depthFunc(515);
            UGraphics.disableDepth();

            UGraphics.enableBlend();
            RenderUtil.defaultBlendFunc();
            UGraphics.depthMask(true);
            //#if MC>=11903
            UGraphics.bindTexture(0, particle.getSprite().atlasLocation());
            //#else
            //$$ UGraphics.bindTexture(0, particle.getSprite().atlas().location());
            //#endif

            buffer.beginWithDefaultShader(
                    UGraphics.DrawMode.QUADS,
                    UGraphics.CommonVertexFormats.POSITION_TEXTURE_COLOR
            );

            particle.buildGeometry(stack, buffer, delta);

            buffer.drawDirect();
        }
    }
}
