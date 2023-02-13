package su.plo.lib.mod.client.chat;

import gg.essential.universal.wrappers.message.UTextComponent;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.chat.MinecraftTextComponent;
import su.plo.lib.api.chat.MinecraftTextConverter;
import su.plo.lib.api.chat.MinecraftTranslatableText;
import su.plo.lib.api.chat.TranslatableTextConverter;
import su.plo.lib.mod.chat.ComponentTextConverter;
import su.plo.lib.mod.client.language.LanguageUtil;

@RequiredArgsConstructor
public final class ClientTextConverter extends TranslatableTextConverter<UTextComponent> {

    private final MinecraftTextConverter<UTextComponent> textConverter = new ComponentTextConverter();
    @Setter
    private ClientLanguageSupplier languageSupplier;

    @Override
    public UTextComponent convert(@NotNull MinecraftTextComponent text) {
        if (!(text instanceof MinecraftTranslatableText) || languageSupplier == null)
            return textConverter.convert(text);

        return languageSupplier.get()
                .map((language) -> {
                    MinecraftTranslatableText translatable = translateArguments(language, (MinecraftTranslatableText) text);

                    if (!language.containsKey(translatable.getKey()))
                        return textConverter.convert(translatable);

                    if (LanguageUtil.has(translatable.getKey()))
                        return textConverter.convert(translatable);

                    return textConverter.convert(translate(language, translatable));
                })
                .orElseGet(() -> textConverter.convert(text));
    }
}
