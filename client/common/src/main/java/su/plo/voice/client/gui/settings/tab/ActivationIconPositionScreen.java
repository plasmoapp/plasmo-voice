package su.plo.voice.client.gui.settings.tab;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.config.entry.EnumConfigEntry;
import su.plo.lib.chat.TextComponent;
import su.plo.lib.client.MinecraftClientLib;
import su.plo.lib.client.gui.components.Button;
import su.plo.lib.client.gui.screen.GuiScreen;
import su.plo.voice.client.config.IconPosition;
import su.plo.voice.client.gui.settings.HudPositionScreen;

public final class ActivationIconPositionScreen extends HudPositionScreen<IconPosition> {

    private final IconPosition disabledPosition;

    public ActivationIconPositionScreen(@NotNull MinecraftClientLib minecraft,
                                        @NotNull GuiScreen parent,
                                        @NotNull EnumConfigEntry<IconPosition> entry,
                                        @Nullable IconPosition disabledPosition) {
        super(minecraft, parent, entry, TextComponent.translatable("gui.plasmovoice.overlay.activation_icon_position.choose"));

        this.disabledPosition = disabledPosition;
    }

    @Override
    public void init() {
        super.init();

        int width = screen.getWidth();
        int height = screen.getHeight();

        addRenderWidget(createPositionButton(
                BUTTON_OFFSET,
                BUTTON_OFFSET,
                IconPosition.TOP_LEFT
        ));

        addRenderWidget(createPositionButton(
                (width / 2) - (BUTTON_WIDTH / 2),
                BUTTON_OFFSET,
                IconPosition.TOP_CENTER
        ));

        addRenderWidget(createPositionButton(
                width - BUTTON_OFFSET - BUTTON_WIDTH,
                BUTTON_OFFSET,
                IconPosition.TOP_RIGHT
        ));


        addRenderWidget(createPositionButton(
                BUTTON_OFFSET,
                height - 20 - BUTTON_OFFSET,
                IconPosition.BOTTOM_LEFT
        ));

        addRenderWidget(createPositionButton(
                (width / 2) - (BUTTON_WIDTH / 2),
                height - 20 - BUTTON_OFFSET,
                IconPosition.BOTTOM_CENTER
        ));

        addRenderWidget(createPositionButton(
                width - BUTTON_OFFSET - BUTTON_WIDTH,
                height - 20 - BUTTON_OFFSET,
                IconPosition.BOTTOM_RIGHT
        ));
    }

    @Override
    protected Button createPositionButton(int x, int y, IconPosition iconPosition) {
        Button button = new Button(
                minecraft,
                x,
                y,
                BUTTON_WIDTH,
                20,
                TextComponent.translatable(iconPosition.getTranslation()),
                (btn) -> {
                    entry.set(iconPosition);
                    minecraft.setScreen(parent);
                },
                Button.NO_TOOLTIP
        );

        button.setActive(iconPosition != disabledPosition);
        return button;
    }
}
