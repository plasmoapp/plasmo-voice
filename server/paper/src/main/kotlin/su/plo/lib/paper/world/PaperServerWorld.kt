package su.plo.lib.paper.world

import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.entity.Entity
import su.plo.lib.api.server.entity.MinecraftServerEntity
import su.plo.lib.api.server.world.MinecraftServerWorld
import su.plo.lib.paper.world.GameEventUtil.parseGameEvent
import java.util.*

class PaperServerWorld(
    private val level: World
) : MinecraftServerWorld {

    override fun getKey(): String {
        return level.key.toString()
    }

    override fun sendGameEvent(entity: MinecraftServerEntity, gameEvent: String) {
        val majorMinecraftVersion = Bukkit.getMinecraftVersion().substringBefore(".").toInt()
        if (majorMinecraftVersion < 19) return

        val paperEntity = entity.getInstance<Entity>()
        level.sendGameEvent(paperEntity, parseGameEvent(gameEvent)!!, paperEntity.location.toVector())
    }

    override fun <T> getInstance() = level as T

    override fun equals(other: Any?) =
        if (this === other) {
            true
        } else if (other != null && this.javaClass == other.javaClass) {
            val world = other as PaperServerWorld
            level === world.level
        } else {
            false
        }

    override fun hashCode() = Objects.hash(level)
}
