package su.plo.voice.client.gui.settings.tab;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.config.entry.EnumConfigEntry;
import su.plo.lib.mod.client.gui.components.Button;
import su.plo.lib.mod.client.gui.screen.ScreenWrapper;
import su.plo.slib.api.chat.component.McTextComponent;
import su.plo.voice.api.client.config.overlay.OverlayPosition;
import su.plo.voice.client.gui.settings.HudPositionScreen;
import su.plo.voice.client.gui.settings.VoiceSettingsScreen;

public final class OverlayPositionScreen extends HudPositionScreen<OverlayPosition> {

    private final OverlayPosition disabledPosition;

    public OverlayPositionScreen(
            @NotNull VoiceSettingsScreen parent,
            @NotNull EnumConfigEntry<OverlayPosition> entry,
            @Nullable OverlayPosition disabledPosition
    ) {
        super(parent, entry, McTextComponent.translatable("gui.plasmovoice.overlay.position.choose"));

        this.disabledPosition = disabledPosition;
    }

    @Override
    public void init() {
        super.init();

        int width = getWidth();
        int height = getHeight();

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
    protected Button createPositionButton(int x, int y, OverlayPosition overlayPosition) {
        Button button = new Button(
                x,
                y,
                BUTTON_WIDTH,
                20,
                McTextComponent.translatable(overlayPosition.getTranslation()),
                (btn) -> {
                    entry.set(overlayPosition);
                    ScreenWrapper.openScreen(parent);
                },
                Button.NO_TOOLTIP
        );

        button.setActive(overlayPosition != disabledPosition);
        return button;
    }
}
