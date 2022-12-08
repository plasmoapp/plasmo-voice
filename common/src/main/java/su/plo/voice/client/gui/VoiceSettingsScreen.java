package su.plo.voice.client.gui;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.Blocks;
import org.lwjgl.glfw.GLFW;
import su.plo.voice.client.VoiceClient;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.client.gui.particle.BlockDustParticle2D;
import su.plo.voice.client.gui.tabs.*;
import su.plo.voice.client.gui.widgets.MicrophoneThresholdWidget;
import su.plo.voice.client.gui.widgets.TooltipImageButton;
import su.plo.voice.client.socket.SocketClientUDPQueue;
import su.plo.voice.client.sound.Compressor;
import su.plo.voice.client.sound.openal.CustomSource;
import su.plo.voice.client.utils.AudioUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class VoiceSettingsScreen extends Screen {
    private static final int minWidth = 640;
    private final Minecraft client = Minecraft.getInstance();

    // tabs
    private TabWidget aboutWidget;
    private int active;
    private final List<Button> tabButtons = new ArrayList<>();
    private final List<TabWidget> tabWidgets = new ArrayList<>();

    // mute mic button
    private List<Button> muteMicButtons;
    // mute output button
    private List<Button> muteSpeakerButtons;

    // particles
    private final List<BlockDustParticle2D> particles = new ArrayList<>();
    private final Random random = new Random();

    // about feature
    private boolean about;
    private int titleWidth;
    private long lastClick = 0L;
    private int clickCount = 0;

    // mic test
    @Getter
    private double microphoneValue = 0.0D;
    @Getter
    private double microphoneDB = 0.0D;
    @Getter
    private double highestDB = -127.0D;
    private long lastUpdate = 0L;

    @Setter
    @Getter
    private CustomSource source;

    @Getter
    private final Compressor compressor = new Compressor();

    // tooltips
    @Setter
    private List<Component> tooltip;

    public VoiceSettingsScreen() {
        super(getTranslatedTitle());
        AboutTabWidget.DeveloperEntry.loadSkins();
    }

    private static Component getTranslatedTitle() {
        Component title = Component.translatable(
                "gui.plasmo_voice.title",
                "Plasmo Voice " + VoiceClient.getInstance().getVersion()
        );
        Language language = Language.getInstance();

        if (!language.getOrDefault("gui.plasmo_voice.title").contains("%s")) {
            return Component.literal("Plasmo Voice ")
                    .append(Component.literal(VoiceClient.getInstance().getVersion()))
                    .append(Component.literal(" Settings"));
        }

        return title;
    }

    private float randomFloat(float min, float max) {
        return min + random.nextFloat() * (max - min);
    }

    public TabWidget getActiveTab() {
        return about
                ? aboutWidget
                : this.tabWidgets.size() > 0
                ? this.tabWidgets.get(active)
                : null;
    }

    public void setMicrophoneValue(byte[] buffer) {
        if (VoiceClient.getClientConfig().compressor.get()) {
            buffer = compressor.compress(buffer);
        }

        microphoneDB = AudioUtils.getHighestAudioLevel(buffer);
        if (microphoneDB > highestDB) {
            highestDB = microphoneDB;
            lastUpdate = System.currentTimeMillis();
        } else if (System.currentTimeMillis() - lastUpdate > 1000L) {
            highestDB = microphoneDB;
        }
        double value = 1 - (microphoneDB / -60);

        if (microphoneDB > -60 && value > microphoneValue) {
            // -60 0
            microphoneValue = 1 - (microphoneDB / -60);
        } else {
            microphoneValue = Math.max(microphoneValue - 0.02D, 0.0F);
        }

        if (source != null) {
            byte[] finalBuffer = buffer;
            VoiceClient.getSoundEngine().runInContext(() -> {
                source.setVolume(VoiceClient.getClientConfig().voiceVolume.get().floatValue());
                source.write(finalBuffer);
            });
        }
    }

    @Override
    public void tick() {
        List<BlockDustParticle2D> toRemove = new ArrayList<>();
        for (BlockDustParticle2D particle : particles) {
            particle.tick();

            if (!particle.isAlive()) {
                toRemove.add(particle);
            }
        }

        particles.removeAll(toRemove);

        if (System.currentTimeMillis() - lastClick > 5000L) {
            clickCount = 0;
        }
    }

    @Override
    public void removed() {
        super.removed();

        VoiceClient.getSoundEngine().runInContext(() -> {
            source.close();
            this.source = null;
        });

        VoiceClient.getClientConfig().save();
    }

    @Override
    protected void init() {
        super.init();

        ClientConfig config = VoiceClient.getClientConfig();

        this.titleWidth = font.width(title);
        clearWidgets();
        // todo optimization
        addTab(Component.translatable("gui.plasmo_voice.general"), new GeneralTabWidget(client, this));
        addTab(Component.translatable("gui.plasmo_voice.advanced"), new AdvancedTabWidget(client, this));
        addTab(Component.translatable("gui.plasmo_voice.hotkeys"), new KeyBindingsTabWidget(client, this));
        aboutWidget = new AboutTabWidget(client, this);
//        active = tabButtons.get(0);

        // mute mic
        ImageButton muteMicHide = new TooltipImageButton(this, this.width - 52, 8, 20, 20, 0, 32, 20,
                VoiceClient.ICONS, 256, 256, button -> {
            minecraft.tell(() -> {
                this.muteMicButtons.get(0).visible = false;
                this.muteMicButtons.get(1).visible = true;
                config.microphoneMuted.set(true);
            });
        }, () -> ImmutableList.of(
                Component.translatable("gui.plasmo_voice.toggle.microphone"),
                Component.translatable("gui.plasmo_voice.toggle.currently",
                        Component.translatable("gui.plasmo_voice.toggle.enabled").withStyle(ChatFormatting.GREEN)
                ).withStyle(ChatFormatting.GRAY)
        ));

        ImageButton muteMicShow = new TooltipImageButton(this, this.width - 52, 8, 20, 20, 20, 32, 20,
                VoiceClient.ICONS, 256, 256, button -> {
            minecraft.tell(() -> {
                this.muteMicButtons.get(0).visible = true;
                this.muteMicButtons.get(1).visible = false;
                config.microphoneMuted.set(false);

                if (config.speakerMuted.get()) {
                    this.muteSpeakerButtons.get(0).visible = true;
                    this.muteSpeakerButtons.get(1).visible = false;
                    config.speakerMuted.set(false);
                }
            });
        }, () -> ImmutableList.of(
                Component.translatable("gui.plasmo_voice.toggle.microphone"),
                Component.translatable("gui.plasmo_voice.toggle.currently",
                        Component.translatable("gui.plasmo_voice.toggle.disabled").withStyle(ChatFormatting.RED)
                ).withStyle(ChatFormatting.GRAY)
        ));

        muteMicHide.visible = !config.microphoneMuted.get() && !config.speakerMuted.get();
        muteMicShow.visible = config.microphoneMuted.get() || config.speakerMuted.get();

        // mute speaker
        ImageButton muteSpeakerHide = new TooltipImageButton(this, this.width - 28, 8, 20, 20, 0, 72, 20,
                VoiceClient.ICONS, 256, 256, button -> {
            minecraft.tell(() -> {
                this.muteSpeakerButtons.get(0).visible = false;
                this.muteSpeakerButtons.get(1).visible = true;
                config.speakerMuted.invert();

                SocketClientUDPQueue.closeAll();

                if (!config.microphoneMuted.get()) {
                    this.muteMicButtons.get(0).visible = false;
                    this.muteMicButtons.get(1).visible = true;
                }
            });
        }, () -> ImmutableList.of(
                Component.translatable("gui.plasmo_voice.toggle.voice"),
                Component.translatable("gui.plasmo_voice.toggle.currently",
                        Component.translatable("gui.plasmo_voice.toggle.enabled").withStyle(ChatFormatting.GREEN)
                ).withStyle(ChatFormatting.GRAY)
        ));

        ImageButton muteSpeakerShow = new TooltipImageButton(this, this.width - 28, 8, 20, 20, 20, 72, 20,
                VoiceClient.ICONS, 256, 256, button -> {
            minecraft.tell(() -> {
                this.muteSpeakerButtons.get(0).visible = true;
                this.muteSpeakerButtons.get(1).visible = false;
                config.speakerMuted.invert();

                if (!config.microphoneMuted.get()) {
                    this.muteMicButtons.get(0).visible = true;
                    this.muteMicButtons.get(1).visible = false;
                }
            });
        }, () -> ImmutableList.of(
                Component.translatable("gui.plasmo_voice.toggle.voice"),
                Component.translatable("gui.plasmo_voice.toggle.currently",
                        Component.translatable("gui.plasmo_voice.toggle.disabled").withStyle(ChatFormatting.RED)
                ).withStyle(ChatFormatting.GRAY)
        ));

        muteSpeakerHide.visible = !config.speakerMuted.get();
        muteSpeakerShow.visible = config.speakerMuted.get();

        this.muteMicButtons = ImmutableList.of(muteMicHide, muteMicShow);
        this.muteSpeakerButtons = ImmutableList.of(muteSpeakerHide, muteSpeakerShow);
    }

    public void updateGeneralTab() {
        this.tabWidgets.set(0, new GeneralTabWidget(client, this));
    }

    public void closeSpeaker() {
        for (TabWidget tab : tabWidgets) {
            for (TabWidget.Entry entry : tab.children()) {
                if (entry instanceof TabWidget.OptionEntry &&
                        entry.children().get(0) instanceof MicrophoneThresholdWidget microphoneTest) {
                    microphoneTest.closeSpeaker();
                }
            }
        }
    }

    private void addTab(Component text, TabWidget drawable) {
        int textWidth = font.width(text) + 16;
        final int elementIndex = tabWidgets.size();
        Button button = Button.builder(text, btn -> {
            active = elementIndex;
            about = false;
            for (TabWidget widget : tabWidgets) {
                widget.onClose();
            }
            aboutWidget.setScrollAmount(0);

            this.closeSpeaker();
        }).width(textWidth).build();
        tabButtons.add(button);
        tabWidgets.add(drawable);
    }

    public int getHeaderHeight() {
        if (this.width < minWidth) {
            return 64;
        } else {
            return 36;
        }
    }

    private void renderParticles(float delta) {
        for (BlockDustParticle2D particle : particles) {
            Tesselator tessellator = Tesselator.getInstance();
            BufferBuilder bufferBuilder = tessellator.getBuilder();
            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            RenderSystem.depthFunc(515);
            RenderSystem.disableDepthTest();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.depthMask(true);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

            particle.buildGeometry(bufferBuilder, delta);

            tessellator.end();
        }
    }

    public void renderBackground(PoseStack matrices) {
        int height = getHeaderHeight();

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, BACKGROUND_LOCATION);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferBuilder.vertex(0.0D, height, 0.0D).uv(0.0F, (float) height / 32.0F + 0).color(64, 64, 64, 255).endVertex();
        bufferBuilder.vertex(this.width, height, 0.0D).uv((float) this.width / 32.0F, (float) height / 32.0F + 0).color(64, 64, 64, 255).endVertex();
        bufferBuilder.vertex(this.width, 0.0D, 0.0D).uv((float) this.width / 32.0F, 0).color(64, 64, 64, 255).endVertex();
        bufferBuilder.vertex(0.0D, 0.0D, 0.0D).uv(0.0F, 0).color(64, 64, 64, 255).endVertex();
        tesselator.end();


        RenderSystem.depthFunc(515);
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
        RenderSystem.disableTexture();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.vertex(0, height + 4, 0.0D).color(0, 0, 0, 0).endVertex();
        bufferBuilder.vertex(this.width, height + 4, 0.0D).color(0, 0, 0, 0).endVertex();
        bufferBuilder.vertex(this.width, height, 0.0D).color(0, 0, 0, 255).endVertex(); // chanje alfa hire to 125
        bufferBuilder.vertex(0, height, 0.0D).color(0, 0, 0, 255).endVertex();       // chanje alfa hire to 125
        tesselator.end();
    }

    public void renderHeader(PoseStack matrices, int mouseX, int mouseY, float delta) {
        font.drawShadow(matrices, title, 14, 15, 16777215);

        // render tabs buttons
        int buttonX = 14;
        int buttonY = 36;

        if (this.width >= minWidth) {
            int buttonsWidth = (tabButtons.size() - 1) * 4;
            for (Button button : tabButtons) {
                buttonsWidth += button.getWidth();
            }

            buttonX = (this.width / 2) - (buttonsWidth / 2);
            buttonY = 8;
        }

        for (int i = 0; i < tabButtons.size(); i++) {
            Button button = tabButtons.get(i);
            button.active = about || i != active;

            button.setX(buttonX);
            buttonX += button.getWidth() + 4;
            button.setY(buttonY);

            RenderSystem.enableDepthTest();
            RenderSystem.depthFunc(519);
            button.render(matrices, mouseX, mouseY, delta);
        }

        for (Button button : this.muteMicButtons) {
            button.render(matrices, mouseX, mouseY, delta);
        }

        for (Button button : this.muteSpeakerButtons) {
            button.render(matrices, mouseX, mouseY, delta);
        }
    }

    @Override
    protected void clearWidgets() {
        super.clearWidgets();
        this.tabButtons.clear();
        this.tabWidgets.clear();
        this.muteMicButtons = null;
        this.muteSpeakerButtons = null;
        this.aboutWidget = null;
    }

    @Override
    public List<? extends GuiEventListener> children() {
        List<GuiEventListener> list = new ArrayList<>(super.children());
        list.addAll(this.tabButtons);
        if (this.muteMicButtons != null) {
            list.addAll(this.muteMicButtons);
        }
        if (this.muteSpeakerButtons != null) {
            list.addAll(this.muteSpeakerButtons);
        }
        if (this.tabWidgets.size() == 0) {
            return list;
        }
        if (!about) {
            list.add(this.tabWidgets.get(active));
        } else {
            list.add(this.aboutWidget);
        }
        return list;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE ||
                keyCode == GLFW.GLFW_KEY_TAB) {
            for (TabWidget widget : tabWidgets) {
                if (widget instanceof KeyBindingsTabWidget) {
                    if (widget.keyPressed(keyCode, scanCode, modifiers)) {
                        return true;
                    }
                }
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.tabWidgets.get(active) instanceof KeyBindingsTabWidget) {
            this.tabWidgets.get(active).mouseReleased(mouseX, mouseY, button);
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (mouseX >= 14 && mouseX <= (14 + titleWidth) &&
                    mouseY >= 15 && mouseY <= (15 + font.lineHeight) &&
                    !about
            ) {
                if (clickCount > 10) {
                    about = true;
                    lastClick = 0L;
                    clickCount = 0;
                    client.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    return true;
                }

                lastClick = System.currentTimeMillis();
                clickCount++;

                client.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.GRAVEL_HIT, 0.0F));

                for (int i = 0; i < 2 + random.nextInt(3); i++) {
                    BlockDustParticle2D particle = new BlockDustParticle2D(14 + randomFloat(0, titleWidth),15 + randomFloat(0, font.lineHeight),
                            1, -3, Blocks.DIRT.defaultBlockState());
                    particle.setMaxAge(10 + random.nextInt(25));
                    particle.setVelocity(randomFloat(-0.25f, 0.25f), -0.25f);
                    particle.setGravityStrength(4.0f);
                    particle.setScale(randomFloat(1.5f, 2.5f));
                    particles.add(particle);
                }
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        this.tooltip = null;
        super.renderBackground(matrices);
        if (about) {
            this.aboutWidget.render(matrices, mouseX, mouseY, delta);
        } else {
            this.tabWidgets.get(active).render(matrices, mouseX, mouseY, delta);
        }
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);

        this.renderHeader(matrices, mouseX, mouseY, delta);
        this.renderParticles(delta);

        if (this.tooltip != null) {
            this.renderComponentTooltip(matrices, this.tooltip, mouseX, mouseY);
        }

//        this.drawString(matrices, minecraft.font, roflanDebugText, 16, 64, 16777215);
    }
}
