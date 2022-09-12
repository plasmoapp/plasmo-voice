package su.plo.voice.chat;

import org.jetbrains.annotations.NotNull;

public interface TextConverter<T> {

    T convert(@NotNull Text text);
}
