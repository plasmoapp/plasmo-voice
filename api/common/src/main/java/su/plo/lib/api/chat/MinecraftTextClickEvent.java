package su.plo.lib.api.chat;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

@Data
@Accessors(fluent = true)
public final class MinecraftTextClickEvent {

    public static @NotNull MinecraftTextClickEvent openUrl(@NotNull String url) {
        return new MinecraftTextClickEvent(Action.OPEN_URL, url);
    }

    public static @NotNull MinecraftTextClickEvent runCommand(@NotNull String command) {
        return new MinecraftTextClickEvent(Action.RUN_COMMAND, command);
    }

    public static @NotNull MinecraftTextClickEvent suggestCommand(@NotNull String command) {
        return new MinecraftTextClickEvent(Action.SUGGEST_COMMAND, command);
    }

    public static @NotNull MinecraftTextClickEvent changePage(@NotNull String page) {
        return new MinecraftTextClickEvent(Action.CHANGE_PAGE, page);
    }

    public static @NotNull MinecraftTextClickEvent copyToClipboard(@NotNull String text) {
        return new MinecraftTextClickEvent(Action.COPY_TO_CLIPBOARD, text);
    }

    public static @NotNull MinecraftTextClickEvent clickEvent(@NotNull Action action, @NotNull String value) {
        return new MinecraftTextClickEvent(action, value);
    }

    private final @NotNull Action action;
    private final @NotNull String value;

    private MinecraftTextClickEvent(@NonNull Action action, @NonNull String value) {
        this.action = action;
        this.value = value;
    }

    @Accessors(fluent = true)
    public enum Action {

        OPEN_URL,
        OPEN_FILE,
        RUN_COMMAND,
        SUGGEST_COMMAND,
        CHANGE_PAGE,
        COPY_TO_CLIPBOARD
    }
}
