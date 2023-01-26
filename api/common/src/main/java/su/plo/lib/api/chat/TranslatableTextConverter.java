package su.plo.lib.api.chat;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class TranslatableTextConverter<T> implements MinecraftTextConverter<T> {

    protected MinecraftTranslatableText translateArguments(@NotNull Map<String, String> language,
                                                           @NotNull MinecraftTranslatableText text) {
        List<Object> translated = Arrays.stream(text.getArgs())
                .map((argument) -> {
                    if (!(argument instanceof MinecraftTranslatableText)) return argument;

                    MinecraftTranslatableText translatable = (MinecraftTranslatableText) argument;
                    if (!language.containsKey(translatable.getKey())) return argument;

                    return translate(language, translatable);
                })
                .collect(Collectors.toList());

        return new MinecraftTranslatableText(text.getKey(), translated.toArray(new Object[0]));
    }

    protected MinecraftTextComponent translate(@NotNull Map<String, String> language,
                                               @NotNull MinecraftTranslatableText translatable) {
        return MinecraftTextComponent.literal(String.format(
                language.get(translatable.getKey()),
                translatable.getArgs()
        ));
    }
}
