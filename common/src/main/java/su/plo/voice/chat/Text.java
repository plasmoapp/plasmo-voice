package su.plo.voice.chat;

public abstract class Text {

    public static Text literal(String text) {
        return new LiteralText(text);
    }

    public static Text translatable(String key, Object ...args) {
        return new TranslatableText(key, args);
    }
}
