package su.plo.voice.client.gui.settings;

import org.jetbrains.annotations.NotNull;
import su.plo.config.entry.EnumConfigEntry;
import su.plo.lib.api.chat.TextComponent;
import su.plo.lib.api.client.MinecraftClientLib;
import su.plo.lib.api.client.gui.GuiRender;
import su.plo.lib.api.client.gui.components.Button;
import su.plo.lib.api.client.gui.screen.GuiScreen;

public abstract class HudPositionScreen<E extends Enum<E>> extends GuiScreen {

    protected static final int BUTTON_OFFSET = 25;
    protected static final int BUTTON_WIDTH = 100;

    protected final GuiScreen parent;
    protected final EnumConfigEntry<E> entry;
    protected final TextComponent chooseText;

    public HudPositionScreen(@NotNull MinecraftClientLib minecraft,
                             @NotNull GuiScreen parent,
                             @NotNull EnumConfigEntry<E> entry,
                             @NotNull TextComponent chooseText) {
        super(minecraft);

        this.parent = parent;
        this.entry = entry;
        this.chooseText = chooseText;
    }

    @Override
    public void render(@NotNull GuiRender render, int mouseX, int mouseY, float delta) {
        screen.renderBackground();
        super.render(render, mouseX, mouseY, delta);

        render.drawString(
                chooseText,
                screen.getWidth() / 2 - minecraft.getFont().width(chooseText) / 2,
                screen.getHeight() / 2 - minecraft.getFont().getLineHeight(),
                16777215
        );
    }

    @Override
    public boolean onClose() {
        minecraft.setScreen(parent);
        return true;
    }

    protected abstract Button createPositionButton(int x, int y, E iconPosition);

}
