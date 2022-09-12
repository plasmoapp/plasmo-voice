package su.plo.voice.chat;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public final class ComponentTextConverter implements TextConverter<Component> {

    @Override
    public Component convert(@NotNull Text text) {
        if (text instanceof TranslatableText)
            return convertTranslatable((TranslatableText) text);

        return Component.literal(text.toString());
    }

    private Component convertTranslatable(@NotNull TranslatableText text) {
        Object[] args = new Object[text.getArgs().length];

        for (int i = 0; i < args.length; i++) {
            Object arg = text.getArgs()[i];

            if (arg instanceof TranslatableText) {
                args[i] = convertTranslatable((TranslatableText) arg);
            } else {
                args[i] = arg;
            }
        }

        return Component.translatable(text.getKey(), args);
    }
}
