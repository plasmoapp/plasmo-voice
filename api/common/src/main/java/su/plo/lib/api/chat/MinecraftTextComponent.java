package su.plo.lib.api.chat;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class MinecraftTextComponent {

    @Getter
    protected final List<MinecraftTextStyle> styles = new ArrayList<>();
    @Getter
    protected final List<MinecraftTextComponent> siblings = new ArrayList<>();

    public static MinecraftLiteralText literal(String text) {
        return new MinecraftLiteralText(text);
    }

    public static MinecraftTranslatableText translatable(String key, Object ...args) {
        return new MinecraftTranslatableText(key, args);
    }

    public static MinecraftTextComponent empty() {
        return new MinecraftLiteralText("");
    }

    public MinecraftTextComponent append(@NotNull MinecraftTextComponent text) {
        siblings.add(text);
        return this;
    }

    public MinecraftTextComponent withStyle(@NotNull MinecraftTextStyle style) {
        styles.add(style);
        return this;
    }

    public MinecraftTextComponent withStyle(@NotNull MinecraftTextStyle...styles) {
        this.styles.addAll(Arrays.asList(styles));
        return this;
    }
}
