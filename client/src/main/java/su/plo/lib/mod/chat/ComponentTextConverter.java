package su.plo.lib.mod.chat;

import gg.essential.universal.wrappers.message.UTextComponent;
import lombok.NonNull;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.lib.api.chat.*;

import java.util.List;

public final class ComponentTextConverter implements MinecraftTextConverter<UTextComponent> {

    @Override
    public UTextComponent convert(@NotNull MinecraftTextComponent text) {
        UTextComponent component;

        if (text instanceof MinecraftTranslatableText)
            component = convertTranslatable((MinecraftTranslatableText) text);
        else
            component = new UTextComponent(Component.literal(text.toString()));

        // apply styles
        component = applyStyles(component, text.styles());

        // apply click event
        component = applyClickEvent(component, text.clickEvent());

        // apply hover event
        component = applyHoverEvent(component, text.hoverEvent());

        // add siblings
        for (MinecraftTextComponent sibling : text.siblings()) {
            component = new UTextComponent(component.appendSibling(convert(sibling)));
        }

        return new UTextComponent(component);
    }

    private UTextComponent convertTranslatable(@NotNull MinecraftTranslatableText text) {
        Object[] args = new Object[text.getArgs().length];

        for (int i = 0; i < args.length; i++) {
            Object arg = text.getArgs()[i];

            if (arg instanceof MinecraftTextComponent) {
                args[i] = convert((MinecraftTextComponent) arg);
            } else {
                args[i] = arg;
            }
        }

        return new UTextComponent(Component.translatable(text.getKey(), args));
    }

    private UTextComponent applyClickEvent(@NotNull UTextComponent component,
                                           @Nullable MinecraftTextClickEvent clickEvent) {
        if (clickEvent == null) return component;

        return component.setClick(
                ClickEvent.Action.valueOf(clickEvent.action().name()),
                clickEvent.value()
        );
    }

    private UTextComponent applyHoverEvent(@NotNull UTextComponent component,
                                           @Nullable MinecraftTextHoverEvent hoverEvent) {
        if (hoverEvent == null) return component;

        // todo: waytoodank
        if (hoverEvent.action() == MinecraftTextHoverEvent.Action.SHOW_TEXT) {
            return component.setHover(
                    HoverEvent.Action.SHOW_TEXT,
                    convert((MinecraftTextComponent) hoverEvent.value()).getComponent()
            );
        }

        return component;
    }

    private UTextComponent applyStyles(@NonNull UTextComponent component,
                                       @NotNull List<MinecraftTextStyle> styles) {
        if (styles.isEmpty()) return component;

        // todo: legacy support
        component.getComponent().setStyle(component.getStyle().applyFormats(
                styles.stream()
                        .map(this::convertStyle)
                        .toArray(ChatFormatting[]::new)
        ));

        return component;
    }

    private ChatFormatting convertStyle(@NotNull MinecraftTextStyle style) {
        return ChatFormatting.valueOf(style.name());
    }
}
