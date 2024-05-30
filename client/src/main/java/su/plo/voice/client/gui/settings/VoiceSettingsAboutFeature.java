package su.plo.voice.client.gui.settings;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.mod.client.render.RenderUtil;
import su.plo.lib.mod.client.render.VertexFormatMode;
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

            SoundManager soundManager = Minecraft.getInstance().getSoundManager();
            soundManager.play(
                    SimpleSoundInstance.forUI(
                            //#if MC>=11903
                            SoundEvents.UI_BUTTON_CLICK.value(),
                            //#else
                            //$$ SoundEvents.UI_BUTTON_CLICK,
                            //#endif
                            1.0f,
                            0.25f
                    )
            );

            return;
        }

        this.lastClick = System.currentTimeMillis();
        this.clickCount++;

        SoundManager soundManager = Minecraft.getInstance().getSoundManager();
        soundManager.play(
                SimpleSoundInstance.forUI(SoundEvents.GRAVEL_HIT, 1f, 1f)
        );

        for (int i = 0; i < 2 + RandomUtil.randomInt(3); i++) {
            BlockDustParticle2D particle = new BlockDustParticle2D(
                    14 + RandomUtil.randomInt(parent.getTitleWidth()),
                    15 + RandomUtil.randomInt(RenderUtil.getFontHeight()),
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

    public void render(@NotNull PoseStack stack, float delta) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();

        for (BlockDustParticle2D particle : particles) {
//            render.setShader(VertexBuilder.Shader.POSITION_TEX_COLOR);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            RenderSystem.depthFunc(515);
            RenderSystem.disableDepthTest();

            RenderSystem.enableBlend();
            RenderUtil.defaultBlendFunc();
            RenderSystem.depthMask(true);
            //#if MC>=11903
            RenderUtil.bindTexture(0, particle.getSprite().atlasLocation());
            //#else
            //$$ RenderUtil.bindTexture(0, particle.getSprite().atlas().location());
            //#endif

            RenderUtil.beginBufferWithDefaultShader(buffer, VertexFormatMode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

            particle.buildGeometry(stack, buffer, delta);

            tesselator.end();
        }
    }
}
