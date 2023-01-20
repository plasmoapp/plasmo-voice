package su.plo.lib.velocity.chat;

import com.google.common.collect.Lists;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.chat.MinecraftTextComponent;
import su.plo.lib.api.chat.MinecraftTextConverter;
import su.plo.lib.api.chat.MinecraftTextStyle;
import su.plo.lib.api.chat.MinecraftTranslatableText;

import java.util.List;

public final class ComponentTextConverter implements MinecraftTextConverter<Component> {

    @Override
    public Component convert(@NotNull MinecraftTextComponent text) {
        Component component;

        if (text instanceof MinecraftTranslatableText)
            component = convertTranslatable((MinecraftTranslatableText) text);
        else
            component = Component.text(text.toString());

        // apply styles
        component.style(getStyles(text));

        // add siblings
        for (MinecraftTextComponent sibling : text.getSiblings()) {
            component.append(convert(sibling));
        }

        return component;
    }

    private Component convertTranslatable(@NotNull MinecraftTranslatableText text) {
        List<Component> args = Lists.newArrayList();

        for (int i = 0; i < text.getArgs().length; i++) {
            Object arg = text.getArgs()[i];

            if (arg instanceof MinecraftTextComponent) {
                args.add(convert((MinecraftTextComponent) arg));
            } else {
                args.add(Component.text(arg.toString()));
            }
        }

        return Component.translatable(text.getKey(), args);
    }

    private Style getStyles(@NotNull MinecraftTextComponent text) {
        Style.Builder builder = Style.style();
        text.getStyles().forEach((style) -> convertStyle(builder, style));
        return builder.build();
    }

    private Style.Builder convertStyle(@NotNull Style.Builder builder, @NotNull MinecraftTextStyle style) {
        if (style.getType() == MinecraftTextStyle.Type.COLOR) {
            builder.color(NamedTextColor.NAMES.value(style.name()));
        } else if (style.getType() == MinecraftTextStyle.Type.DECORATION) {
            builder.decoration(TextDecoration.NAMES.value(style.name()), true);
        }

        return builder;
    }
}
