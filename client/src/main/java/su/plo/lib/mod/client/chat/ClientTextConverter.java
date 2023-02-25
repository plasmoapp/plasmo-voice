package su.plo.lib.mod.client.chat;

import gg.essential.universal.wrappers.message.UTextComponent;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.chat.MinecraftTextComponent;
import su.plo.lib.api.chat.MinecraftTextConverter;
import su.plo.lib.api.chat.MinecraftTranslatableText;
import su.plo.lib.api.chat.TranslatableTextConverter;
import su.plo.lib.mod.chat.ComponentTextConverter;
import su.plo.lib.mod.client.language.LanguageUtil;

@RequiredArgsConstructor
public final class ClientTextConverter extends TranslatableTextConverter<Component> {

    private final MinecraftTextConverter<Component> textConverter = new ComponentTextConverter();
    @Setter
    private ClientLanguageSupplier languageSupplier;

    @Override
    public Component convert(@NotNull MinecraftTextComponent text) {
        if (!(text instanceof MinecraftTranslatableText) || languageSupplier == null)
            return textConverter.convert(text);

        return languageSupplier.get()
                .map((language) -> {
                    MinecraftTextComponent translatedText = translateInner(language, text);

                    if (!(translatedText instanceof MinecraftTranslatableText))
                        return textConverter.convert(translatedText);

                    MinecraftTranslatableText translatable = (MinecraftTranslatableText) translatedText;

                    if (!language.containsKey(translatable.getKey()))
                        return textConverter.convert(translatable);

                    if (LanguageUtil.has(translatable.getKey()))
                        return textConverter.convert(translatable);

                    return textConverter.convert(translate(language, translatable));
                })
                .orElseGet(() -> textConverter.convert(text));
    }

    public UTextComponent convertToUniversal(@NotNull MinecraftTextComponent text) {
        return new UTextComponent(convert(text));
    }
}
