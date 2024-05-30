package gg.essential.universal.wrappers

import net.minecraft.client.Minecraft
import net.minecraft.client.player.LocalPlayer

/**
 * Backward compat with pv-addon-soundphysics, not actually used in PV.
 */
object UPlayer {
    @JvmStatic
    fun getPlayer(): LocalPlayer? =
        Minecraft.getInstance().player

    @JvmStatic
    fun hasPlayer() = getPlayer() != null

    @JvmStatic
    fun getPosX(): Double {
        return getPlayer()?.x
            ?: throw NullPointerException("UPlayer.getPosX() called with no existing Player")
    }

    @JvmStatic
    fun getPosY(): Double {
        return getPlayer()?.y
            ?: throw NullPointerException("UPlayer.getPosY() called with no existing Player")
    }

    @JvmStatic
    fun getPosZ(): Double {
        return getPlayer()?.z
            ?: throw NullPointerException("UPlayer.getPosZ() called with no existing Player")
    }

    @JvmStatic
    fun getPrevPosX(): Double {
        return getPlayer()?.xOld
            ?: throw NullPointerException("UPlayer.getPrevPosX() called with no existing Player")
    }

    @JvmStatic
    fun getPrevPosY(): Double {
        return getPlayer()?.yOld
            ?: throw NullPointerException("UPlayer.getPrevPosY() called with no existing Player")
    }

    @JvmStatic
    fun getPrevPosZ(): Double {
        return getPlayer()?.zOld
            ?: throw NullPointerException("UPlayer.getPrevPosZ() called with no existing Player")
    }
}
