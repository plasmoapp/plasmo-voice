package su.plo.lib.paper.chat;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.chat.MinecraftTextComponent;
import su.plo.lib.api.chat.MinecraftTextConverter;
import su.plo.lib.api.chat.MinecraftTranslatableText;

public class BaseComponentTextConverter implements MinecraftTextConverter<BaseComponent> {

    @Override
    public BaseComponent convert(@NotNull MinecraftTextComponent text) {
        BaseComponent component;

        if (text instanceof MinecraftTranslatableText)
            component = convertTranslatable((MinecraftTranslatableText) text);
        else
            component = new TextComponent(text.toString());

        // apply styles
        applyStyles(component, text);

        // add siblings
        for (MinecraftTextComponent sibling : text.getSiblings()) {
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

    private void applyStyles(@NotNull BaseComponent component,
                             @NotNull MinecraftTextComponent text) {
        text.getStyles().forEach(
                (style) -> component.setColor(ChatColor.of(style.name()))
        );
    }
}
