package su.plo.lib.mod.client.render

import java.awt.Color

object Colors {
    @JvmField
    val WHITE = Color.WHITE
    @JvmField
    val BLACK = Color.BLACK
    @JvmField
    val GRAY = Color(0xA0A0A0)

    @JvmField
    val TEXT = Color(0xE0E0E0)
    @JvmField
    val TEXT_DISABLED = Color(0x707070)
    @JvmField
    val TEXT_MUTED = Color(0x808080)
    @JvmField
    val BACKGROUND = Color(0x404040)
    @JvmField
    val WARNING = Color(0xFAC653)

    @JvmStatic
    fun Color.withAlpha(alpha: Int): Color =
        Color(red, green, blue, alpha)

    @JvmStatic
    fun Color.withAlpha(alpha: Float): Color =
        Color(red, green, blue, (alpha * 255f).toInt())

    @JvmStatic
    operator fun Color.times(scalar: Double): Color =
        Color(
            (red * scalar).toInt().coerceIn(0, 255),
            (green * scalar).toInt().coerceIn(0, 255),
            (blue * scalar).toInt().coerceIn(0, 255),
            alpha
        )
}
