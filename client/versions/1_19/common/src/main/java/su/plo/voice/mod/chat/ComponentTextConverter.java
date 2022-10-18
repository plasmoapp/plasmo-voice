package su.plo.voice.mod.chat;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.chat.MinecraftTextComponent;
import su.plo.lib.api.chat.MinecraftTextConverter;
import su.plo.lib.api.chat.MinecraftTextStyle;
import su.plo.lib.api.chat.MinecraftTranslatableText;

public final class ComponentTextConverter implements MinecraftTextConverter<Component> {

    @Override
    public Component convert(@NotNull MinecraftTextComponent text) {
        MutableComponent component;

        if (text instanceof MinecraftTranslatableText)
            component = convertTranslatable((MinecraftTranslatableText) text);
        else
            component = Component.literal(text.toString());

        // apply styles
        component.withStyle(getStyles(text));

        // add siblings
        for (MinecraftTextComponent sibling : text.getSiblings()) {
            component.append(convert(sibling));
        }

        return component;
    }

    private MutableComponent convertTranslatable(@NotNull MinecraftTranslatableText text) {
        Object[] args = new Object[text.getArgs().length];

        for (int i = 0; i < args.length; i++) {
            Object arg = text.getArgs()[i];

            if (arg instanceof MinecraftTextComponent) {
                args[i] = convert((MinecraftTextComponent) arg);
            } else {
                args[i] = arg;
            }
        }

        return Component.translatable(text.getKey(), args);
    }

    private ChatFormatting[] getStyles(@NotNull MinecraftTextComponent text) {
        return text.getStyles()
                .stream()
                .map(this::convertStyle)
                .toArray(ChatFormatting[]::new);
    }

    private ChatFormatting convertStyle(@NotNull MinecraftTextStyle style) {
        return ChatFormatting.valueOf(style.name());
    }
}
