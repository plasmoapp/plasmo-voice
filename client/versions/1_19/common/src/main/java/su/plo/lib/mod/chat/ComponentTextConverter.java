package su.plo.lib.mod.chat;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.lib.api.chat.*;

public final class ComponentTextConverter implements MinecraftTextConverter<Component> {

    @Override
    public Component convert(@NotNull MinecraftTextComponent text) {
        MutableComponent component;

        if (text instanceof MinecraftTranslatableText)
            component = convertTranslatable((MinecraftTranslatableText) text);
        else
            component = Component.literal(text.toString());

        // apply styles
        component = component.withStyle(getStyles(text));

        // apply click event
        component = applyClickEvent(component, text.clickEvent());

        // apply hover event
        component = applyHoverEvent(component, text.hoverEvent());

        // add siblings
        for (MinecraftTextComponent sibling : text.siblings()) {
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

    private MutableComponent applyClickEvent(@NotNull MutableComponent component,
                                             @Nullable MinecraftTextClickEvent clickEvent) {
        if (clickEvent == null) return component;

        return component.withStyle(component.getStyle().withClickEvent(new ClickEvent(
                ClickEvent.Action.valueOf(clickEvent.action().name()),
                clickEvent.value()
        )));
    }

    private MutableComponent applyHoverEvent(@NotNull MutableComponent component,
                                             @Nullable MinecraftTextHoverEvent hoverEvent) {
        if (hoverEvent == null) return component;

        // todo: waytoodank
        if (hoverEvent.action() == MinecraftTextHoverEvent.Action.SHOW_TEXT) {
            return component.withStyle(component.getStyle().withHoverEvent(new HoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    convert((MinecraftTextComponent) hoverEvent.value())
            )));
        }

        return component;
    }

    private ChatFormatting[] getStyles(@NotNull MinecraftTextComponent text) {
        return text.styles()
                .stream()
                .map(this::convertStyle)
                .toArray(ChatFormatting[]::new);
    }

    private ChatFormatting convertStyle(@NotNull MinecraftTextStyle style) {
        return ChatFormatting.valueOf(style.name());
    }
}
