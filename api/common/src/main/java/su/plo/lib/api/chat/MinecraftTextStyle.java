package su.plo.lib.api.chat;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public enum MinecraftTextStyle {

    BLACK(Type.COLOR),
    DARK_BLUE(Type.COLOR),
    DARK_GREEN(Type.COLOR),
    DARK_AQUA(Type.COLOR),
    DARK_RED(Type.COLOR),
    DARK_PURPLE(Type.COLOR),
    GOLD(Type.COLOR),
    GRAY(Type.COLOR),
    DARK_GRAY(Type.COLOR),
    BLUE(Type.COLOR),
    GREEN(Type.COLOR),
    AQUA(Type.COLOR),
    RED(Type.COLOR),
    LIGHT_PURPLE(Type.COLOR),
    YELLOW(Type.COLOR),
    WHITE(Type.COLOR),
    OBFUSCATED(Type.DECORATION),
    BOLD(Type.DECORATION),
    STRIKETHROUGH(Type.DECORATION),
    UNDERLINE(Type.DECORATION),
    ITALIC(Type.DECORATION),
    RESET(Type.RESET);

    @Getter
    private final Type type;

    MinecraftTextStyle(@NotNull Type type) {
        this.type = type;
    }

    public enum Type {
        COLOR,
        DECORATION,
        RESET // ??
    }
}
