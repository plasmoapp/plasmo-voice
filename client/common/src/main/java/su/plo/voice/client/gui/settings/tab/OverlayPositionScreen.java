package su.plo.voice.client.gui.settings.tab;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.config.entry.EnumConfigEntry;
import su.plo.lib.chat.TextComponent;
import su.plo.lib.client.MinecraftClientLib;
import su.plo.lib.client.gui.components.Button;
import su.plo.lib.client.gui.screen.GuiScreen;
import su.plo.voice.client.config.overlay.OverlayPosition;
import su.plo.voice.client.gui.settings.HudPositionScreen;

public final class OverlayPositionScreen extends HudPositionScreen<OverlayPosition> {

    private final OverlayPosition dislabledPosition;

    public OverlayPositionScreen(@NotNull MinecraftClientLib minecraft,
                                 @NotNull GuiScreen parent,
                                 @NotNull EnumConfigEntry<OverlayPosition> entry,
                                 @Nullable OverlayPosition disabledPosition) {
        super(minecraft, parent, entry, TextComponent.translatable("gui.plasmovoice.overlay.position.choose"));

        this.dislabledPosition = disabledPosition;
    }

    @Override
    public void init() {
        super.init();

        int width = screen.getWidth();
        int height = screen.getHeight();

        addRenderWidget(createPositionButton(
                BUTTON_OFFSET,
                BUTTON_OFFSET,
                OverlayPosition.TOP_LEFT
        ));


        addRenderWidget(createPositionButton(
                width - BUTTON_OFFSET - BUTTON_WIDTH,
                BUTTON_OFFSET,
                OverlayPosition.TOP_RIGHT
        ));


        addRenderWidget(createPositionButton(
                BUTTON_OFFSET,
                height - 20 - BUTTON_OFFSET,
                OverlayPosition.BOTTOM_LEFT
        ));

        addRenderWidget(createPositionButton(
                width - BUTTON_OFFSET - BUTTON_WIDTH,
                height - 20 - BUTTON_OFFSET,
                OverlayPosition.BOTTOM_RIGHT
        ));
    }

    @Override
    protected Button createPositionButton(int x, int y, OverlayPosition iconPosition) {
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

        button.setActive(iconPosition != dislabledPosition);
        return button;
    }
}
