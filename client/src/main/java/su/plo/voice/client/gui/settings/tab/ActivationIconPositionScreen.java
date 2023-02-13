package su.plo.voice.client.gui.settings.tab;

import gg.essential.universal.UScreen;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.config.entry.EnumConfigEntry;
import su.plo.lib.api.chat.MinecraftTextComponent;
import su.plo.lib.mod.client.gui.components.Button;
import su.plo.lib.mod.client.gui.screen.GuiScreen;
import su.plo.lib.mod.client.gui.screen.ScreenWrapper;
import su.plo.voice.client.config.IconPosition;
import su.plo.voice.client.gui.settings.HudPositionScreen;

public final class ActivationIconPositionScreen extends HudPositionScreen<IconPosition> {

    private final IconPosition disabledPosition;

    public ActivationIconPositionScreen(@NotNull GuiScreen parent,
                                        @NotNull EnumConfigEntry<IconPosition> entry,
                                        @Nullable IconPosition disabledPosition) {
        super(parent, entry, MinecraftTextComponent.translatable("gui.plasmovoice.overlay.activation_icon_position.choose"));

        this.disabledPosition = disabledPosition;
    }

    @Override
    public void init() {
        super.init();

        int width = screen.width;
        int height = screen.height;

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
                x,
                y,
                BUTTON_WIDTH,
                20,
                MinecraftTextComponent.translatable(iconPosition.getTranslation()),
                (btn) -> {
                    entry.set(iconPosition);
                    ScreenWrapper.openScreen(parent);
                },
                Button.NO_TOOLTIP
        );

        button.setActive(iconPosition != disabledPosition);
        return button;
    }
}
