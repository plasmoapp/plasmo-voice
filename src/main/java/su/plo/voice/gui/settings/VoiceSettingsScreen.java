package su.plo.voice.gui.settings;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.AbstractButton;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import su.plo.voice.Voice;
import su.plo.voice.gui.BackgroundScreen;
import su.plo.voice.utils.Utils;

import java.util.concurrent.atomic.AtomicReference;

public class VoiceSettingsScreen extends BackgroundScreen {
    public static final ResourceLocation TEXTURE = new ResourceLocation("plasmo_voice", "textures/gui/settings.png");
    private final Minecraft client;
    private short tab = 0;
    private final AtomicReference<AbstractButton> general = new AtomicReference<>();
    private final AtomicReference<AbstractButton> audio = new AtomicReference<>();

    private MicTestButton testButton;
    private double mic;

    public VoiceSettingsScreen() {
        super(new TranslationTextComponent("gui.plasmo_voice.title"), 248, 180, TEXTURE, false);
        this.client = Minecraft.getInstance();

        Voice.config.save();
    }

    @Override
    public void removed() {
        super.removed();

        Voice.config.save();
        testButton.close();
    }

    @Override
    protected void init() {
        super.init();

        this.testButton = new MicTestButton(guiLeft + 10, guiTop + 165, xSize - 20, 20,
                (mic) -> this.mic = mic);

        // tabs
        general.set(new Button(guiLeft + 10, guiTop + 10, (xSize / 2) - 12, 20,
                new TranslationTextComponent("gui.plasmo_voice.general"), button -> {
            button.active = false;
            if(audio.get() != null) {
                audio.get().active = true;
            }
            this.renderTab0();
        }));
        if(this.tab == 0) {
            general.get().active = false;
        }

        audio.set(new Button(guiLeft + 10 + general.get().getWidth() + 4, guiTop + 10, (xSize / 2) - 12, 20,
                new TranslationTextComponent("gui.plasmo_voice.audio"), button -> {
            button.active = false;
            if(general.get() != null) {
                general.get().active = true;
            }
            this.renderTab1();
        }));
        if(this.tab == 1) {
            audio.get().active = false;
        }

        if(this.tab == 0) {
            renderTab0();
        } else {
            renderTab1();
        }
    }

    private void renderTab0() {
        this.tab = 0;
        this.ySize = 175;
        this.buttons.clear();
        this.children.clear();

        addButton(general.get());
        addButton(audio.get());

        addButton(new VoiceVolumeSlider(guiLeft + 10, guiTop + 40, xSize - 20));

        addButton(new Button(guiLeft + 10, guiTop + 65, xSize - 20, 20,
                new TranslationTextComponent("gui.plasmo_voice.occlusion").append(": ").append(onOff(Voice.config.occlusion, "gui.plasmo_voice.on", "gui.plasmo_voice.off")), button -> {
            Voice.config.occlusion = !Voice.config.occlusion;
            button.setMessage(new TranslationTextComponent("gui.plasmo_voice.occlusion").append(": ").append(onOff(Voice.config.occlusion, "gui.plasmo_voice.on", "gui.plasmo_voice.off")));
        }));

        addButton(new Button(guiLeft + 10, guiTop + 90, xSize - 20, 20,
                new TranslationTextComponent("gui.plasmo_voice.show_icons").append(": ").append(onOff(Voice.config.showIcons, new String[]{"gui.plasmo_voice.show_icons_hud", "gui.plasmo_voice.show_icons_always", "gui.plasmo_voice.show_icons_hidden"})), button -> {
            if(Voice.config.showIcons == 0) {
                Voice.config.showIcons = 1;
            } else if(Voice.config.showIcons == 1) {
                Voice.config.showIcons = 2;
            } else if(Voice.config.showIcons == 2) {
                Voice.config.showIcons = 0;
            }
            button.setMessage(new TranslationTextComponent("gui.plasmo_voice.show_icons").append(": ").append(onOff(Voice.config.showIcons, new String[]{"gui.plasmo_voice.show_icons_hud", "gui.plasmo_voice.show_icons_always", "gui.plasmo_voice.show_icons_hidden"})));
        }));

        addButton(new DistanceSlider(guiLeft + 10, guiTop + 115, xSize - 20));

        addButton(new Button(guiLeft + 10, guiTop + 145, xSize - 20, 20, new TranslationTextComponent("gui.plasmo_voice.close"), button -> {
            client.setScreen(null);
        }));
    }

    private void renderTab1() {
        this.tab = 1;
        this.ySize = 225;
        this.buttons.clear();
        this.children.clear();

        addButton(general.get());
        addButton(audio.get());

        addButton(new MicAmplificationSlider(guiLeft + 10, guiTop + 40, xSize - 20));

        addButton(new Button(general.get().x, guiTop + 65, general.get().getWidth(), 20,
                new TranslationTextComponent("gui.plasmo_voice.select_mic"), button -> {
            client.setScreen(new MicSelectScreen(this));
        }));

        addButton(new Button(audio.get().x, guiTop + 65, audio.get().getWidth(), 20,
                new TranslationTextComponent("gui.plasmo_voice.select_speaker"), button -> {
            client.setScreen(new SpeakerSelectScreen(this));
        }));

        Button activation = new Button(guiLeft + 10, guiTop + 90, xSize - 20, 20,
                new TranslationTextComponent("gui.plasmo_voice.activation_type").append(": ")
                        .append(onOff(Voice.config.voiceActivation && !Voice.serverConfig.disableVoiceActivation,
                                "gui.plasmo_voice.activation_type_voice", "gui.plasmo_voice.activation_type_ptt")), button -> {
            Voice.config.voiceActivation = !Voice.config.voiceActivation;
            button.setMessage(new TranslationTextComponent("gui.plasmo_voice.activation_type").append(": ")
                    .append(onOff(Voice.config.voiceActivation && !Voice.serverConfig.disableVoiceActivation,
                            "gui.plasmo_voice.activation_type_voice", "gui.plasmo_voice.activation_type_ptt")));
        });

        if(Voice.serverConfig.disableVoiceActivation) {
            activation.active = false;
        }

        addButton(activation);

        addButton(new VoiceActivationSlider(guiLeft + 10, guiTop + 115, xSize - 20));
        addButton(this.testButton);

        addButton(new Button(guiLeft + 10, guiTop + 195, xSize - 20, 20, new TranslationTextComponent("gui.plasmo_voice.close"), button -> {
            client.setScreen(null);
        }));
    }

    private TranslationTextComponent onOff(boolean b, String on, String off) {
        return b ? new TranslationTextComponent(on) : new TranslationTextComponent(off);
    }

    private TranslationTextComponent onOff(int index, String[] variants) {
        return new TranslationTextComponent(variants[index]);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);

        if(tab == 1) {
            client.getTextureManager().bind(TEXTURE);
            blit(matrices, guiLeft + 10, guiTop + 140, 0, 218, xSize - 20, 20, 512, 512);
            blit(matrices, guiLeft + 11, guiTop + 141, 0, 200, (int) ((xSize - 18) * mic), 18, 512, 512);

            if(Voice.config.voiceActivation && !Voice.serverConfig.disableVoiceActivation) {
                int pos = (int) ((xSize - 21) * Utils.dbToPerc(Voice.config.voiceActivationThreshold));
                blit(matrices, guiLeft + 10 + pos, guiTop + 140, 0, 218, 1, 20, 512, 512);
            }
        }
    }
}
