package su.plo.lib.api.client.gui.narration;

import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.chat.TextComponent;

public interface NarrationOutput {

    void add(@NotNull Type narratedElementType, @NotNull TextComponent component);

    NarrationOutput nest();

    enum Type {
        TITLE,
        POSITION,
        HINT,
        USAGE
    }
}
