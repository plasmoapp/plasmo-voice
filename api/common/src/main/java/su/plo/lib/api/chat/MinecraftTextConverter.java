package su.plo.lib.api.chat;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public interface MinecraftTextConverter<T> {

    T convert(@NotNull MinecraftTextComponent text);

    default List<T> convert(@NotNull List<MinecraftTextComponent> list) {
        return list.stream()
                .map(this::convert)
                .collect(Collectors.toList());
    }
}
