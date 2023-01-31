package su.plo.lib.paper.chat;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.lib.api.chat.MinecraftTextClickEvent;
import su.plo.lib.api.chat.MinecraftTextComponent;
import su.plo.lib.api.chat.MinecraftTextHoverEvent;
import su.plo.lib.api.chat.MinecraftTranslatableText;
import su.plo.lib.api.server.chat.ServerTextConverter;
import su.plo.voice.api.server.config.ServerLanguages;

import java.util.function.Supplier;

public class BaseComponentTextConverter extends ServerTextConverter<BaseComponent> {

    public BaseComponentTextConverter(Supplier<ServerLanguages> languages) {
        super(languages);
    }

    @Override
    public BaseComponent convert(@NotNull MinecraftTextComponent text) {
        BaseComponent component;

        if (text instanceof MinecraftTranslatableText)
            component = convertTranslatable((MinecraftTranslatableText) text);
        else
            component = new TextComponent(text.toString());

        // apply styles
        applyStyles(component, text);

        // apply click event
        applyClickEvent(component, text.clickEvent());

        // apply hover event
        applyHoverEvent(component, text.hoverEvent());

        // add siblings
        for (MinecraftTextComponent sibling : text.siblings()) {
            component.addExtra(convert(sibling));
        }

        return component;
    }

    private BaseComponent convertTranslatable(@NotNull MinecraftTranslatableText text) {
        Object[] args = new Object[text.getArgs().length];

        for (int i = 0; i < args.length; i++) {
            Object arg = text.getArgs()[i];

            if (arg instanceof MinecraftTextComponent) {
                args[i] = convert((MinecraftTextComponent) arg);
            } else {
                args[i] = arg;
            }
        }

        return new TranslatableComponent(text.getKey(), args);
    }

    private void applyClickEvent(@NotNull BaseComponent component,
                                 @Nullable MinecraftTextClickEvent clickEvent) {
        if (clickEvent == null) return;

        component.setClickEvent(new ClickEvent(
                ClickEvent.Action.valueOf(clickEvent.action().name()),
                clickEvent.value()
        ));
    }

    private void applyHoverEvent(@NotNull BaseComponent component,
                                 @Nullable MinecraftTextHoverEvent hoverEvent) {
        if (hoverEvent == null) return;

        if (hoverEvent.action() == MinecraftTextHoverEvent.Action.SHOW_TEXT) {
            component.setHoverEvent(new HoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder(convert((MinecraftTextComponent) hoverEvent.value())).create()
            ));
        }
    }

    private void applyStyles(@NotNull BaseComponent component,
                             @NotNull MinecraftTextComponent text) {
        text.styles().forEach(
                (style) -> component.setColor(ChatColor.of(style.name()))
        );
    }
}
