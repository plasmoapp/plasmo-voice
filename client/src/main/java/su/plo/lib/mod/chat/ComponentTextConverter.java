package su.plo.lib.mod.chat;

import lombok.NonNull;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.lib.api.chat.*;

import java.util.List;

public final class ComponentTextConverter implements MinecraftTextConverter<Component> {

    @Override // todo: legacy
    public @NotNull String convertToJson(@NotNull Component text) {
        return Component.Serializer.toJson(text);
    }

    @Override // todo: legacy
    public Component convertFromJson(@NotNull String json) {
        return Component.Serializer.fromJson(json);
    }

    @Override
    public Component convert(@NotNull MinecraftTextComponent text) {
        MutableComponent component;

        if (text instanceof MinecraftTranslatableText)
            component = convertTranslatable((MinecraftTranslatableText) text);
        else
            //#if MC>=11900
            component = Component.literal(text.toString());
            //#else
            //$$ component = new TextComponent(text.toString());
            //#endif

        // apply styles
        component = applyStyles(component, text.styles());

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

        //#if MC>=11900
        return Component.translatable(text.getKey(), args);
        //#else
        //$$ return new TranslatableComponent(text.getKey(), args);
        //#endif
    }

    private MutableComponent applyClickEvent(@NotNull MutableComponent component,
                                             @Nullable MinecraftTextClickEvent clickEvent) {
        if (clickEvent == null) return component;

        component.setStyle(component.getStyle().withClickEvent(new ClickEvent(
                ClickEvent.Action.valueOf(clickEvent.action().name()),
                clickEvent.value()
        )));

        return component;
    }

    private MutableComponent applyHoverEvent(@NotNull MutableComponent component,
                                             @Nullable MinecraftTextHoverEvent hoverEvent) {
        if (hoverEvent == null) return component;

        // todo: waytoodank
        if (hoverEvent.action() == MinecraftTextHoverEvent.Action.SHOW_TEXT) {
            component.setStyle(component.getStyle().withHoverEvent(new HoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    convert((MinecraftTextComponent) hoverEvent.value())
            )));
        }

        return component;
    }

    private MutableComponent applyStyles(@NonNull MutableComponent component,
                                         @NotNull List<MinecraftTextStyle> styles) {
        if (styles.isEmpty()) return component;

        // todo: legacy support
        component.setStyle(component.getStyle().applyFormats(
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
