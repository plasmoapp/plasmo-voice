package su.plo.voice.mod.chat;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.chat.TextComponent;
import su.plo.lib.api.chat.TextConverter;
import su.plo.lib.api.chat.TextStyle;
import su.plo.lib.api.chat.TranslatableText;

import java.util.List;
import java.util.stream.Collectors;

public final class ComponentTextConverter implements TextConverter<Component> {

    @Override
    public Component convert(@NotNull TextComponent text) {
        MutableComponent component;

        if (text instanceof TranslatableText)
            component = convertTranslatable((TranslatableText) text);
        else
            component = Component.literal(text.toString());

        // apply styles
        component.withStyle(getStyles(text));

        // add siblings
        for (TextComponent sibling : text.getSiblings()) {
            component.append(convert(sibling));
        }

        return component;
    }

    @Override
    public List<Component> convert(@NotNull List<TextComponent> list) {
        return list.stream()
                .map(this::convert)
                .collect(Collectors.toList());
    }

    private MutableComponent convertTranslatable(@NotNull TranslatableText text) {
        Object[] args = new Object[text.getArgs().length];

        for (int i = 0; i < args.length; i++) {
            Object arg = text.getArgs()[i];

            if (arg instanceof TextComponent) {
                args[i] = convert((TextComponent) arg);
            } else {
                args[i] = arg;
            }
        }

        return Component.translatable(text.getKey(), args);
    }

    private ChatFormatting[] getStyles(@NotNull TextComponent text) {
        return text.getStyles()
                .stream()
                .map(this::convertStyle)
                .toArray(ChatFormatting[]::new);
    }

    private ChatFormatting convertStyle(@NotNull TextStyle style) {
        return ChatFormatting.valueOf(style.name());
    }
}
