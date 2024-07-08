package su.plo.lib.mod.client.chat;

import lombok.Setter;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.lib.mod.client.language.LanguageUtil;
import su.plo.slib.api.chat.component.McTextComponent;
import su.plo.slib.api.chat.converter.McTextConverter;
import su.plo.slib.chat.AdventureComponentTextConverter;
import su.plo.slib.language.LegacyComponentRenderer;
import su.plo.slib.libs.adventure.adventure.key.Key;
import su.plo.slib.libs.adventure.adventure.text.TranslatableComponent;
import su.plo.slib.libs.adventure.adventure.text.renderer.TranslatableComponentRenderer;
import su.plo.slib.libs.adventure.adventure.translation.Translator;
import su.plo.slib.mod.chat.ComponentTextConverter;

import java.text.MessageFormat;
import java.util.Locale;

public final class ClientTextConverter implements McTextConverter<Component> {

    private final AdventureComponentTextConverter adventureTextConverter = new AdventureComponentTextConverter();
    private final ComponentTextConverter textConverter = ComponentTextConverter.INSTANCE;
    private final TranslatableComponentRenderer<Locale> textRenderer;

    @Setter
    private ClientLanguageSupplier languageSupplier;

    public ClientTextConverter() {
        Translator translator = new Translator() {
            @Override
            public @NotNull Key name() {
                return Key.key("plasmo", "voice/v2/client/translator");
            }

            @Override
            public su.plo.slib.libs.adventure.adventure.text.@Nullable Component translate(@NotNull TranslatableComponent component, @NotNull Locale locale) {
                if (LanguageUtil.has(component.key())) {
                    return null;
                }

                String translationString = languageSupplier.get()
                        .map((language) -> language.get(component.key()))
                        .orElse(null);
                if (translationString == null) return null;

                return LegacyComponentRenderer.INSTANCE.renderTranslatable(component, translationString, locale, textRenderer);
            }

            @Override
            public @Nullable MessageFormat translate(@NotNull String key, @NotNull Locale locale) {
                return null;
            }
        };

        this.textRenderer = TranslatableComponentRenderer.usingTranslationSource(translator);
    }

    @Override
    public @NotNull String convertToJson(@NotNull Component text) {
        return textConverter.convertToJson(text);
    }

    @Override
    public Component convertFromJson(@NotNull String json) {
        return textConverter.convertFromJson(json);
    }

    @Override
    public Component convert(@NotNull McTextComponent text) {
        String json = adventureTextConverter.convertToJson(
                textRenderer.render(
                        adventureTextConverter.convert(text),
                        Translator.parseLocale("en_us")
                )
        );

        return convertFromJson(json);
    }
}
