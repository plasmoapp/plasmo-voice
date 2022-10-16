package su.plo.voice.client.gui.settings;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.client.MinecraftClientLib;
import su.plo.lib.api.client.gui.GuiRender;
import su.plo.lib.api.client.render.MinecraftTesselator;
import su.plo.lib.api.client.render.VertexBuilder;
import su.plo.lib.api.client.render.particle.MinecraftBlockParticle;
import su.plo.lib.api.client.sound.MinecraftSoundManager;
import su.plo.voice.util.RandomUtil;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public final class VoiceSettingsAboutFeature {

    private static final String LOCATION_BLOCKS = "textures/atlas/blocks.png";

    private final MinecraftClientLib minecraft;
    private final VoiceSettingsScreen parent;

    private final List<MinecraftBlockParticle> particles = Lists.newArrayList();

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

            minecraft.getSoundManager().playSound(
                    MinecraftSoundManager.Category.UI,
                    "minecraft:ui.button.click",
                    1F
            );
            return;
        }

        this.lastClick = System.currentTimeMillis();
        this.clickCount++;

        minecraft.getSoundManager().playSound(
                MinecraftSoundManager.Category.UI,
                "minecraft:block.gravel.hit",
                1F
        );

        for (int i = 0; i < 2 + RandomUtil.randomInt(3); i++) {
            MinecraftBlockParticle particle = minecraft.getSimpleParticles().createBlockParticle(
                    14 + RandomUtil.randomInt(parent.getTitleWidth()),
                    15 + RandomUtil.randomInt(minecraft.getFont().getLineHeight()),
                    "dirt"
            );
            particle.setMaxAge(10 + RandomUtil.randomInt(25));
            particle.setVelocity(RandomUtil.randomFloat(-0.25f, 0.25f), -0.25f);
            particle.setGravityStrength(4.0f);
            particle.setScale(RandomUtil.randomFloat(1.5f, 2.5f));
            particles.add(particle);
        }
    }

    public void render(@NotNull GuiRender render, float delta) {
        MinecraftTesselator tesselator = render.getTesselator();
        VertexBuilder bufferBuilder = tesselator.getBuilder();

        for (MinecraftBlockParticle particle : particles) {
            render.setShader(VertexBuilder.Shader.POSITION_TEX_COLOR);
            render.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            render.depthFunc(515);
            render.disableDepthTest();

            render.enableBlend();
            render.defaultBlendFunc();
            render.depthMask(true);
            render.setShaderTexture(0, LOCATION_BLOCKS);
            bufferBuilder.begin(VertexBuilder.Mode.QUADS, VertexBuilder.Format.POSITION_TEX_COLOR);

            particle.buildGeometry(bufferBuilder, delta);

            tesselator.end();
        }
    }
}
