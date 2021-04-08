package su.plo.voice.gui.settings;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import su.plo.voice.client.VoiceClient;
import su.plo.voice.gui.BackgroundScreen;
import su.plo.voice.utils.Utils;

import java.util.concurrent.atomic.AtomicReference;

public class VoiceSettingsScreen extends BackgroundScreen {
    public static final Identifier TEXTURE = new Identifier("plasmo_voice", "textures/gui/settings.png");
    private final MinecraftClient client;
    private short tab = 0;
    private final AtomicReference<ButtonWidget> general = new AtomicReference<>();
    private final AtomicReference<ButtonWidget> audio = new AtomicReference<>();

    private double mic;

    public VoiceSettingsScreen() {
        super(new TranslatableText("gui.plasmo_voice.title"), 248, 180, TEXTURE, false);
        this.client = MinecraftClient.getInstance();

        VoiceClient.config.save();
    }

    @Override
    public void onClose() {
        super.onClose();
        VoiceClient.config.save();
    }

    @Override
    public void removed() {
        super.removed();
        VoiceClient.config.save();
    }

    @Override
    protected void init() {
        super.init();

        // tabs
        general.set(new ButtonWidget(guiLeft + 10, guiTop + 10, (xSize / 2) - 12, 20,
                new TranslatableText("gui.plasmo_voice.general"), button -> {
            button.active = false;
            if(audio.get() != null) {
                audio.get().active = true;
            }
            this.renderTab0();
        }));
        if(this.tab == 0) {
            general.get().active = false;
        }

        audio.set(new ButtonWidget(guiLeft + 10 + general.get().getWidth() + 4, guiTop + 10, (xSize / 2) - 12, 20,
                new TranslatableText("gui.plasmo_voice.audio"), button -> {
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

//        addButton(new ButtonWidget(guiLeft + 10, guiTop + 70, xSize - 20, 20, new TranslatableText("plasmo_voice.select_mic"), button -> {
//            client.openScreen(new SelectMicrophoneScreen());
//        }));
    }

    private void renderTab0() {
        this.tab = 0;
        this.ySize = 175;
        this.buttons.clear();
        this.children.clear();

        addButton(general.get());
        addButton(audio.get());

        addButton(new VoiceVolumeSlider(guiLeft + 10, guiTop + 40, xSize - 20));

        addButton(new ButtonWidget(guiLeft + 10, guiTop + 65, xSize - 20, 20,
                new TranslatableText("gui.plasmo_voice.occlusion").append(": ").append(onOff(VoiceClient.config.occlusion, "gui.plasmo_voice.on", "gui.plasmo_voice.off")), button -> {
            VoiceClient.config.occlusion = !VoiceClient.config.occlusion;
            button.setMessage(new TranslatableText("gui.plasmo_voice.occlusion").append(": ").append(onOff(VoiceClient.config.occlusion, "gui.plasmo_voice.on", "gui.plasmo_voice.off")));
        }));

        addButton(new ButtonWidget(guiLeft + 10, guiTop + 90, xSize - 20, 20,
                new TranslatableText("gui.plasmo_voice.show_icons").append(": ").append(onOff(VoiceClient.config.showIcons, new String[]{"gui.plasmo_voice.show_icons_hud", "gui.plasmo_voice.show_icons_always", "gui.plasmo_voice.show_icons_hidden"})), button -> {
            if(VoiceClient.config.showIcons == 0) {
                VoiceClient.config.showIcons = 1;
            } else if(VoiceClient.config.showIcons == 1) {
                VoiceClient.config.showIcons = 2;
            } else if(VoiceClient.config.showIcons == 2) {
                VoiceClient.config.showIcons = 0;
            }
            button.setMessage(new TranslatableText("gui.plasmo_voice.show_icons").append(": ").append(onOff(VoiceClient.config.showIcons, new String[]{"gui.plasmo_voice.show_icons_hud", "gui.plasmo_voice.show_icons_always", "gui.plasmo_voice.show_icons_hidden"})));
        }));

        addButton(new DistanceSlider(guiLeft + 10, guiTop + 115, xSize - 20));

        addButton(new ButtonWidget(guiLeft + 10, guiTop + 145, xSize - 20, 20, new TranslatableText("gui.plasmo_voice.close"), button -> {
            client.openScreen(null);
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

        addButton(new ButtonWidget(general.get().x, guiTop + 65, general.get().getWidth(), 20,
                new TranslatableText("gui.plasmo_voice.select_mic"), button -> {
            client.openScreen(new MicSelectScreen(this));
        }));

        addButton(new ButtonWidget(audio.get().x, guiTop + 65, audio.get().getWidth(), 20,
                new TranslatableText("gui.plasmo_voice.select_speaker"), button -> {
            client.openScreen(new SpeakerSelectScreen(this));
        }));

        ButtonWidget activation = new ButtonWidget(guiLeft + 10, guiTop + 90, xSize - 20, 20,
                new TranslatableText("gui.plasmo_voice.activation_type").append(": ")
                        .append(onOff(VoiceClient.config.voiceActivation && !VoiceClient.serverConfig.disableVoiceActivation,
                                "gui.plasmo_voice.activation_type_voice", "gui.plasmo_voice.activation_type_ptt")), button -> {
            VoiceClient.config.voiceActivation = !VoiceClient.config.voiceActivation;
            button.setMessage(new TranslatableText("gui.plasmo_voice.activation_type").append(": ")
                    .append(onOff(VoiceClient.config.voiceActivation && !VoiceClient.serverConfig.disableVoiceActivation,
                            "gui.plasmo_voice.activation_type_voice", "gui.plasmo_voice.activation_type_ptt")));
        });

        if(VoiceClient.serverConfig.disableVoiceActivation) {
            activation.active = false;
        }

        addButton(activation);

        addButton(new VoiceActivationSlider(guiLeft + 10, guiTop + 115, xSize - 20));
        addButton(new MicTestButton(guiLeft + 10, guiTop + 165, xSize - 20, 20,
                (mic) -> this.mic = mic));

        addButton(new ButtonWidget(guiLeft + 10, guiTop + 195, xSize - 20, 20, new TranslatableText("gui.plasmo_voice.close"), button -> {
            client.openScreen(null);
        }));
    }

    private TranslatableText onOff(boolean b, String on, String off) {
        return b ? new TranslatableText(on) : new TranslatableText(off);
    }

    private TranslatableText onOff(int index, String[] variants) {
        return new TranslatableText(variants[index]);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);

        if(tab == 1) {
            client.getTextureManager().bindTexture(TEXTURE);
            drawTexture(matrices, guiLeft + 10, guiTop + 140, 0, 218, xSize - 20, 20, 512, 512);
            drawTexture(matrices, guiLeft + 11, guiTop + 141, 0, 200, (int) ((xSize - 18) * mic), 18, 512, 512);

            if(VoiceClient.config.voiceActivation && !VoiceClient.serverConfig.disableVoiceActivation) {
                int pos = (int) ((xSize - 21) * Utils.dbToPerc(VoiceClient.config.voiceActivationThreshold));
                drawTexture(matrices, guiLeft + 10 + pos, guiTop + 140, 0, 218, 1, 20, 512, 512);
            }
        }
    }
}
