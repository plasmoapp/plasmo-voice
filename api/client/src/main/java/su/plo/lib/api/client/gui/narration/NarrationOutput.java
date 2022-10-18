package su.plo.lib.api.client.gui.narration;

import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.chat.MinecraftTextComponent;

public interface NarrationOutput {

    void add(@NotNull Type narratedElementType, @NotNull MinecraftTextComponent component);

    NarrationOutput nest();

    enum Type {
        TITLE,
        POSITION,
        HINT,
        USAGE
    }
}
