package su.plo.lib.api.server.chat;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.chat.MinecraftTextComponent;
import su.plo.lib.api.chat.MinecraftTranslatableText;
import su.plo.lib.api.chat.TranslatableTextConverter;
import su.plo.lib.api.server.command.MinecraftCommandSource;
import su.plo.voice.api.server.config.ServerLanguages;

import java.util.Map;
import java.util.function.Supplier;

@RequiredArgsConstructor
public abstract class ServerTextConverter<T> extends TranslatableTextConverter<T> {

    private final Supplier<ServerLanguages> languagesSupplier;

    public T convert(@NotNull MinecraftCommandSource source, @NotNull MinecraftTextComponent text) {
        ServerLanguages languages = languagesSupplier.get();
        if (languages == null || !(text instanceof MinecraftTranslatableText))
            return convert(text);

        Map<String, String> language = languages.getServerLanguage(source);
        MinecraftTranslatableText translatable = translateArguments(language, (MinecraftTranslatableText) text);

        if (!language.containsKey(translatable.getKey()))
            return convert(text);

        return convert(translate(language, translatable));
    }
}
