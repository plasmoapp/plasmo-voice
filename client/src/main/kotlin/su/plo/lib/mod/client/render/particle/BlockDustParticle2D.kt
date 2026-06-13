package su.plo.lib.mod.client.render.particle

import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth
import net.minecraft.world.level.block.state.BlockState
import su.plo.lib.mod.client.render.Colors.times
import su.plo.lib.mod.client.render.gui.GuiRenderContext
import java.awt.Color
import kotlin.random.Random

class BlockDustParticle2D(
    private var x: Double,
    private var y: Double,
    state: BlockState,
) {
    private val sprite: TextureAtlasSprite
    private val textureLocation: ResourceLocation
    private val color: Color

    // random offset into the block texture this particle samples from
    private val sampleU = Random.nextFloat() * 3.0f
    private val sampleV = Random.nextFloat() * 3.0f

    private var prevX = x
    private var prevY = y
    private var velocityX = 0.0
    private var velocityY = 0.0
    private var age = 0
    private var dead = false

    var maxAge = 0
    var gravityStrength = 0f
    var scale = 1.0f

    val isAlive: Boolean
        get() = !dead

    init {
        val client = Minecraft.getInstance()

        //#if MC>=26.1
        //$$ sprite = client.modelManager.blockStateModelSet.getParticleMaterial(state).sprite()
        //#else
        sprite = client.blockRenderer.blockModelShaper.getParticleIcon(state)
        //#endif

        textureLocation = sprite.atlasLocation()

        val level = client.level
        val player = client.player
        //#if MC>=26.1
        //$$ val tintSource = client.blockColors.getTintSource(state, 0)
        //$$ val tint = if (tintSource != null && level != null && player != null) {
        //$$     tintSource.colorAsTerrainParticle(state, level, player.blockPosition())
        //$$ } else {
        //$$     -1
        //$$ }
        //#else
        val tint = if (level != null && player != null) {
            client.blockColors.getColor(state, level, player.blockPosition(), 0)
        } else {
            -1
        }
        //#endif

        color = Color(tint) * 0.5
    }

    fun setVelocity(velocityX: Double, velocityY: Double) {
        this.velocityX = velocityX
        this.velocityY = velocityY
    }

    fun tick() {
        prevX = x
        prevY = y
        if (age++ >= maxAge) {
            dead = true
            return
        }

        velocityY += 0.04 * gravityStrength
        x += velocityX
        y += velocityY
        velocityX *= 0.98f
        velocityY *= 0.98f
    }

    fun render(context: GuiRenderContext, tickDelta: Float) {
        val renderX = Mth.lerp(tickDelta.toDouble(), prevX, x).toFloat()
        val renderY = Mth.lerp(tickDelta.toDouble(), prevY, y).toFloat()

        context.blitColor(
            textureLocation,
            (renderX - scale).toInt(),
            (renderX + scale).toInt(),
            (renderY - scale).toInt(),
            (renderY + scale).toInt(),
            minU(), maxU(), minV(), maxV(),
            color,
        )
    }

    private fun minU(): Float = sprite.getU((sampleU + 1.0f) / 4.0f)

    private fun maxU(): Float = sprite.getU(sampleU / 4.0f)

    private fun minV(): Float = sprite.getV(sampleV / 4.0f)

    private fun maxV(): Float = sprite.getV((sampleV + 1.0f) / 4.0f)
}
