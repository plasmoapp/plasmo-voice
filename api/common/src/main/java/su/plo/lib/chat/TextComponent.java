package su.plo.lib.chat;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class TextComponent {

    @Getter
    protected final List<TextStyle> styles = new ArrayList<>();
    @Getter
    protected final List<TextComponent> siblings = new ArrayList<>();

    public static LiteralText literal(String text) {
        return new LiteralText(text);
    }

    public static TranslatableText translatable(String key, Object ...args) {
        return new TranslatableText(key, args);
    }

    public static TextComponent empty() {
        return new LiteralText("");
    }

    public TextComponent append(@NotNull TextComponent text) {
        siblings.add(text);
        return this;
    }

    public TextComponent withStyle(@NotNull TextStyle style) {
        styles.add(style);
        return this;
    }

    public TextComponent withStyle(@NotNull TextStyle ...styles) {
        this.styles.addAll(Arrays.asList(styles));
        return this;
    }
}
