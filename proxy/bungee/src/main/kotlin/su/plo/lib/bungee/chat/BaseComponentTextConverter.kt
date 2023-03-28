package su.plo.lib.bungee.chat

import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.*
import net.md_5.bungee.chat.ComponentSerializer
import su.plo.lib.api.chat.MinecraftTextClickEvent
import su.plo.lib.api.chat.MinecraftTextComponent
import su.plo.lib.api.chat.MinecraftTextHoverEvent
import su.plo.lib.api.chat.MinecraftTranslatableText
import su.plo.lib.api.server.chat.ServerTextConverter
import su.plo.voice.api.server.config.ServerLanguages
import java.util.function.Supplier

class BaseComponentTextConverter(languages: Supplier<ServerLanguages?>) :
    ServerTextConverter<BaseComponent>(languages) {

    override fun convertToJson(text: BaseComponent): String =
        ComponentSerializer.toString(text)

    override fun convertFromJson(json: String): BaseComponent =
        ComponentSerializer.parse(json)[0]

    override fun convert(text: MinecraftTextComponent): BaseComponent {
        val component =
            if (text is MinecraftTranslatableText) convertTranslatable(text)
            else TextComponent(text.toString())

        // apply styles
        applyStyles(component, text)

        // apply click event
        applyClickEvent(component, text.clickEvent())

        // apply hover event
        applyHoverEvent(component, text.hoverEvent())

        // add siblings
        for (sibling in text.siblings()) {
            component.addExtra(convert(sibling))
        }

        return component
    }

    private fun convertTranslatable(text: MinecraftTranslatableText): BaseComponent {
        return TranslatableComponent(
            text.key,
            *text.args.map { argument ->
                if (argument is MinecraftTextComponent) convert(argument)
                else argument
            }.toTypedArray()
        )
    }

    private fun applyClickEvent(
        component: BaseComponent,
        clickEvent: MinecraftTextClickEvent?
    ) {
        if (clickEvent == null) return

        component.clickEvent = ClickEvent(
            ClickEvent.Action.valueOf(clickEvent.action().name),
            clickEvent.value()
        )
    }

    private fun applyHoverEvent(
        component: BaseComponent,
        hoverEvent: MinecraftTextHoverEvent?
    ) {
        if (hoverEvent == null) return

        if (hoverEvent.action() == MinecraftTextHoverEvent.Action.SHOW_TEXT) {
            component.hoverEvent = HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                ComponentBuilder(convert(hoverEvent.value() as MinecraftTextComponent)).create()
            )
        }
    }

    private fun applyStyles(
        component: BaseComponent,
        text: MinecraftTextComponent
    ) {
        text.styles().forEach { style ->
            component.color = ChatColor.of(style.name)
        }
    }
}
