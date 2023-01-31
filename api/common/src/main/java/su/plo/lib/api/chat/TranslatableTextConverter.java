package su.plo.lib.api.chat;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Map;

public abstract class TranslatableTextConverter<T> implements MinecraftTextConverter<T> {

    protected MinecraftTranslatableText translateArguments(@NotNull Map<String, String> language,
                                                           @NotNull MinecraftTranslatableText text) {

        return new MinecraftTranslatableText(text.getKey(), Arrays.stream(text.getArgs())
                .map((argument) -> {
                    if (!(argument instanceof MinecraftTranslatableText)) return argument;

                    MinecraftTranslatableText translatable = (MinecraftTranslatableText) argument;
                    if (!language.containsKey(translatable.getKey())) return argument;

                    return translate(language, translatable);
                }).toArray(Object[]::new));
    }

    protected @Nullable MinecraftTextHoverEvent translate(@NotNull Map<String, String> language,
                                                @Nullable MinecraftTextHoverEvent hoverEvent) {
        if (hoverEvent == null) return null;

        if (hoverEvent.action() == MinecraftTextHoverEvent.Action.SHOW_TEXT) {
            return MinecraftTextHoverEvent.showText(translate(language, (MinecraftTranslatableText) hoverEvent.value()));
        }

        return hoverEvent;
    }

    protected MinecraftTextComponent translate(@NotNull Map<String, String> language,
                                               @NotNull MinecraftTranslatableText translatable) {
        if (Arrays.stream(translatable.getArgs())
                .anyMatch(object -> object instanceof MinecraftTextComponent)
        ) {
            return MinecraftTextComponent.translatable(
                    language.get(translatable.getKey()),
                    translatable.getArgs()
            ).withStyle(translatable.styles().toArray(new MinecraftTextStyle[0]))
                    .clickEvent(translatable.clickEvent())
                    .hoverEvent(translate(language, translatable.hoverEvent()));
        }

        return MinecraftTextComponent.literal(String.format(
                language.get(translatable.getKey()),
                translatable.getArgs()
        )).withStyle(translatable.styles().toArray(new MinecraftTextStyle[0]))
                .clickEvent(translatable.clickEvent())
                .hoverEvent(translate(language, translatable.hoverEvent()));
    }
}
