package su.plo.voice.chat;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface TextConverter<T> {

    T convert(@NotNull TextComponent text);

    List<T> convert(@NotNull List<TextComponent> list);
}
