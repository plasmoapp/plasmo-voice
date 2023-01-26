package su.plo.voice.client.chat;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.chat.MinecraftTextComponent;
import su.plo.lib.api.chat.MinecraftTextConverter;
import su.plo.lib.api.chat.MinecraftTranslatableText;
import su.plo.lib.api.chat.TranslatableTextConverter;
import su.plo.lib.api.client.MinecraftClientLib;

@RequiredArgsConstructor
public abstract class ClientTextConverter<T> extends TranslatableTextConverter<T> {

    private final MinecraftClientLib minecraft;
    private final ClientLanguageSupplier languageSupplier;
    private final MinecraftTextConverter<T> textConverter;

    @Override
    public T convert(@NotNull MinecraftTextComponent text) {
        if (!(text instanceof MinecraftTranslatableText)) return textConverter.convert(text);

        return languageSupplier.get()
                .map((language) -> {
                    MinecraftTranslatableText translatable = translateArguments(language, (MinecraftTranslatableText) text);

                    if (!language.containsKey(translatable.getKey()))
                        return textConverter.convert(translatable);

                    if (minecraft.getLanguage().has(translatable.getKey()))
                        return textConverter.convert(translatable);

                    return textConverter.convert(translate(language, translatable));
                })
                .orElseGet(() -> textConverter.convert(text));
    }
}
