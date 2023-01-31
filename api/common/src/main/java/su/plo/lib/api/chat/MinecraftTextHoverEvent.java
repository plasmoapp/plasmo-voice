package su.plo.lib.api.chat;

import lombok.Data;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

@Data
@Accessors(fluent = true)
public final class MinecraftTextHoverEvent {

    public static @NotNull MinecraftTextHoverEvent showText(@NotNull MinecraftTextComponent text) {
        return new MinecraftTextHoverEvent(Action.SHOW_TEXT, text);
    }

    private @NotNull final Action action;
    private @NotNull final Object value;

    private MinecraftTextHoverEvent(@NotNull Action action, @NotNull Object value) {
        this.action = action;
        this.value = value;
    }

    public enum Action {

        SHOW_TEXT,
//        SHOW_ITEM,
//        SHOW_ENTITY
    }
}
