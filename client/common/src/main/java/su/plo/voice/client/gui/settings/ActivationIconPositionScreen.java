package su.plo.voice.client.gui.settings;

import org.jetbrains.annotations.NotNull;
import su.plo.config.entry.EnumConfigEntry;
import su.plo.lib.client.MinecraftClientLib;
import su.plo.lib.client.gui.GuiRender;
import su.plo.lib.client.gui.components.Button;
import su.plo.lib.client.gui.screen.GuiScreen;
import su.plo.voice.chat.TextComponent;
import su.plo.voice.client.config.ClientConfig;

public final class ActivationIconPositionScreen extends GuiScreen {

    private static final int BUTTON_OFFSET = 25;
    private static final int BUTTON_WIDTH = 100;
    private static final TextComponent CHOOSE_TEXT = TextComponent.translatable("gui.plasmovoice.advanced.icon_position.choose");

    private final GuiScreen parent;
    private final EnumConfigEntry<ClientConfig.Advanced.ActivationIconPosition> entry;

    public ActivationIconPositionScreen(@NotNull MinecraftClientLib minecraft,
                                        @NotNull GuiScreen parent,
                                        @NotNull EnumConfigEntry<ClientConfig.Advanced.ActivationIconPosition> entry) {
        super(minecraft);

        this.parent = parent;
        this.entry = entry;
    }

    @Override
    public void render(@NotNull GuiRender render, int mouseX, int mouseY, float delta) {
        screen.renderBackground();
        super.render(render, mouseX, mouseY, delta);

        render.drawString(
                CHOOSE_TEXT,
                screen.getWidth() / 2 - minecraft.getFont().width(CHOOSE_TEXT) / 2,
                screen.getHeight() / 2 - minecraft.getFont().getLineHeight(),
                16777215
        );
    }

    @Override
    public void init() {
        super.init();

        int width = screen.getWidth();
        int height = screen.getHeight();

        addRenderWidget(createPositionButton(
                BUTTON_OFFSET,
                BUTTON_OFFSET,
                ClientConfig.Advanced.ActivationIconPosition.TOP_LEFT
        ));

        addRenderWidget(createPositionButton(
                (width / 2) - (BUTTON_WIDTH / 2),
                BUTTON_OFFSET,
                ClientConfig.Advanced.ActivationIconPosition.TOP_CENTER
        ));

        addRenderWidget(createPositionButton(
                width - BUTTON_OFFSET - BUTTON_WIDTH,
                BUTTON_OFFSET,
                ClientConfig.Advanced.ActivationIconPosition.TOP_RIGHT
        ));


        addRenderWidget(createPositionButton(
                BUTTON_OFFSET,
                height - 20 - BUTTON_OFFSET,
                ClientConfig.Advanced.ActivationIconPosition.BOTTOM_LEFT
        ));

        addRenderWidget(createPositionButton(
                (width / 2) - (BUTTON_WIDTH / 2),
                height - 20 - BUTTON_OFFSET,
                ClientConfig.Advanced.ActivationIconPosition.BOTTOM_CENTER
        ));

        addRenderWidget(createPositionButton(
                width - BUTTON_OFFSET - BUTTON_WIDTH,
                height - 20 - BUTTON_OFFSET,
                ClientConfig.Advanced.ActivationIconPosition.BOTTOM_RIGHT
        ));
    }

    @Override
    public boolean onClose() {
        minecraft.setScreen(parent);
        return true;
    }

    private Button createPositionButton(int x, int y, ClientConfig.Advanced.ActivationIconPosition iconPosition) {
        Button positionButton = new Button(
                minecraft,
                x,
                y,
                BUTTON_WIDTH,
                20,
                TextComponent.translatable(iconPosition.getTranslation()),
                (button) -> {
                    entry.set(iconPosition);
                    minecraft.setScreen(parent);
                },
                Button.NO_TOOLTIP
        );

        positionButton.setActive(iconPosition != entry.value());
        return positionButton;
    }
}
