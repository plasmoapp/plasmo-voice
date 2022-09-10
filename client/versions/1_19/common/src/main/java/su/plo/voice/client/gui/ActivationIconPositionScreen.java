package su.plo.voice.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import su.plo.config.entry.EnumConfigEntry;
import su.plo.voice.client.config.ClientConfig;

public final class ActivationIconPositionScreen extends Screen {

    private static final int BUTTON_OFFSET = 25;
    private static final int BUTTON_WIDTH = 100;
    private static final Component CHOOSE_TEXT = Component.translatable("gui.plasmovoice.advanced.icon_position.choose");

    private final Screen parent;
    private final EnumConfigEntry<ClientConfig.Advanced.ActivationIconPosition> entry;

    public ActivationIconPositionScreen(Screen parent,
                                        EnumConfigEntry<ClientConfig.Advanced.ActivationIconPosition> entry) {
        super(Component.empty());

        this.parent = parent;
        this.entry = entry;
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float delta) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, delta);

        minecraft.font.draw(
                poseStack,
                CHOOSE_TEXT,
                (float) (width / 2 - minecraft.font.width(CHOOSE_TEXT) / 2),
                (float) height / 2 - minecraft.font.lineHeight,
                16777215
        );
    }

    @Override
    public void onClose() {
        super.onClose();
        minecraft.setScreen(parent);
    }

    @Override
    protected void init() {
        super.init();

        addRenderableWidget(createPositionButton(
                BUTTON_OFFSET,
                BUTTON_OFFSET,
                ClientConfig.Advanced.ActivationIconPosition.TOP_LEFT
        ));

        addRenderableWidget(createPositionButton(
                (width / 2) - (BUTTON_WIDTH / 2),
                BUTTON_OFFSET,
                ClientConfig.Advanced.ActivationIconPosition.TOP_CENTER
        ));

        addRenderableWidget(createPositionButton(
                width - BUTTON_OFFSET - BUTTON_WIDTH,
                BUTTON_OFFSET,
                ClientConfig.Advanced.ActivationIconPosition.TOP_RIGHT
        ));


        addRenderableWidget(createPositionButton(
                BUTTON_OFFSET,
                height - 20 - BUTTON_OFFSET,
                ClientConfig.Advanced.ActivationIconPosition.BOTTOM_LEFT
        ));

        addRenderableWidget(createPositionButton(
                (width / 2) - (BUTTON_WIDTH / 2),
                height - 20 - BUTTON_OFFSET,
                ClientConfig.Advanced.ActivationIconPosition.BOTTOM_CENTER
        ));

        addRenderableWidget(createPositionButton(
                width - BUTTON_OFFSET - BUTTON_WIDTH,
                height - 20 - BUTTON_OFFSET,
                ClientConfig.Advanced.ActivationIconPosition.BOTTOM_RIGHT
        ));
    }

    private Button createPositionButton(int x, int y, ClientConfig.Advanced.ActivationIconPosition iconPosition) {
        return new Button(
                x,
                y,
                BUTTON_WIDTH,
                20,
                Component.translatable(iconPosition.getTranslation()),
                (button) -> {
                    entry.set(iconPosition);
                    minecraft.setScreen(parent);
                }
        );
    }
}
