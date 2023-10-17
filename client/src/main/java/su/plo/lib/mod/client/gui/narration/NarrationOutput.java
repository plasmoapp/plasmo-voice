package su.plo.lib.mod.client.gui.narration;

import org.jetbrains.annotations.NotNull;
import su.plo.slib.api.chat.component.McTextComponent;

public interface NarrationOutput {

    void add(@NotNull Type narratedElementType, @NotNull McTextComponent component);

    NarrationOutput nest();

    enum Type {
        TITLE,
        POSITION,
        HINT,
        USAGE
    }
}
