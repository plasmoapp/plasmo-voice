package su.plo.voice.client.gui.settings.widget

import su.plo.lib.mod.client.gui.components.DisplayWidget
import su.plo.lib.mod.client.render.gui.GuiRenderContext
import su.plo.lib.mod.client.render.texture.ModPlayerSkins
import su.plo.slib.api.entity.player.McGameProfile
import java.util.UUID

class PlayerHeadWidget private constructor(
    private val playerId: UUID,
    private val playerName: String,
    private val profile: McGameProfile?,
    size: Int,
) : DisplayWidget(size, size) {
    constructor(playerId: UUID, playerName: String, size: Int) : this(playerId, playerName, null, size)

    constructor(profile: McGameProfile, size: Int) : this(profile.id, profile.name, profile, size)

    override fun renderButton(context: GuiRenderContext, mouseX: Int, mouseY: Int, delta: Float) {
        profile?.let(ModPlayerSkins::loadSkin)
        val skin = ModPlayerSkins.getSkin(playerId, playerName)

        context.blit(skin, x, y, width, height, 8f, 8f, 8, 8, 64, 64)
        context.blit(skin, x, y, width, height, 40f, 8f, 8, 8, 64, 64)
    }
}
