package su.plo.lib.mod.client.chat;

import su.plo.slib.api.chat.component.McTextComponent;
import su.plo.slib.api.chat.component.McTranslatableText;
import su.plo.slib.api.chat.converter.McTextConverter;
import su.plo.slib.api.chat.converter.TranslatableTextConverter;
import su.plo.slib.mod.chat.ComponentTextConverter;
import su.plo.voice.universal.wrappers.message.UTextComponent;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.mod.client.language.LanguageUtil;

@RequiredArgsConstructor
public final class ClientTextConverter extends TranslatableTextConverter<Component> {

    private final McTextConverter<Component> textConverter = new ComponentTextConverter();
    @Setter
    private ClientLanguageSupplier languageSupplier;

    @Override
    public @NotNull String convertToJson(@NotNull Component text) {
        return Component.Serializer.toJson(text);
    }

    @Override
    public Component convertFromJson(@NotNull String json) {
        return Component.Serializer.fromJson(json);
    }

    @Override
    public Component convert(@NotNull McTextComponent text) {
        if (!(text instanceof McTranslatableText) || languageSupplier == null)
            return textConverter.convert(text);

        return languageSupplier.get()
                .map((language) -> {
                    McTextComponent translatedText = translateInner(language, text);

                    if (!(translatedText instanceof McTranslatableText))
                        return textConverter.convert(translatedText);

                    McTranslatableText translatable = (McTranslatableText) translatedText;

                    if (!language.containsKey(translatable.getKey()))
                        return textConverter.convert(translatable);

                    if (LanguageUtil.has(translatable.getKey()))
                        return textConverter.convert(translatable);

                    return textConverter.convert(translate(language, translatable));
                })
                .orElseGet(() -> textConverter.convert(text));
    }

    public UTextComponent convertToUniversal(@NotNull McTextComponent text) {
        return new UTextComponent(convert(text));
    }
}
