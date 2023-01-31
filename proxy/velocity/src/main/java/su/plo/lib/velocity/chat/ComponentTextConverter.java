package su.plo.lib.velocity.chat;

import com.google.common.collect.Lists;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.lib.api.chat.*;
import su.plo.lib.api.server.chat.ServerTextConverter;
import su.plo.voice.api.server.config.ServerLanguages;

import java.util.List;
import java.util.function.Supplier;

public final class ComponentTextConverter extends ServerTextConverter<Component> {

    public ComponentTextConverter(@NotNull Supplier<ServerLanguages> languagesSupplier) {
        super(languagesSupplier);
    }

    @Override
    public Component convert(@NotNull MinecraftTextComponent text) {
        Component component;

        if (text instanceof MinecraftTranslatableText)
            component = convertTranslatable((MinecraftTranslatableText) text);
        else
            component = Component.text(text.toString());

        // apply styles
        component = component.style(getStyles(text));

        // apply click event
        component = applyClickEvent(component, text.clickEvent());

        // apply hover event
        component = applyHoverEvent(component, text.hoverEvent());

        // add siblings
        for (MinecraftTextComponent sibling : text.siblings()) {
            component = component.append(convert(sibling));
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

    private Component applyClickEvent(@NotNull Component component,
                                      @Nullable MinecraftTextClickEvent clickEvent) {
        if (clickEvent == null) return component;

        return component.clickEvent(ClickEvent.clickEvent(
                ClickEvent.Action.valueOf(clickEvent.action().name()),
                clickEvent.value()
        ));
    }

    private Component applyHoverEvent(@NotNull Component component,
                                      @Nullable MinecraftTextHoverEvent hoverEvent) {
        if (hoverEvent == null) return component;

        // todo: waytoodank
        if (hoverEvent.action() == MinecraftTextHoverEvent.Action.SHOW_TEXT) {
            return component.hoverEvent(HoverEvent.showText(convert((MinecraftTextComponent) hoverEvent.value())));
        }


        return component;
//        return component.hoverEvent(HoverEvent.hoverEvent(
////                HoverEvent.Action.NAMES.value(hoverEvent.action().name()),
//                HoverEvent.Action.SHOW_TEXT,
//                hoverEvent.value()
//        ));
    }

    private Style getStyles(@NotNull MinecraftTextComponent text) {
        Style.Builder builder = Style.style();
        text.styles().forEach((style) -> convertStyle(builder, style));
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
