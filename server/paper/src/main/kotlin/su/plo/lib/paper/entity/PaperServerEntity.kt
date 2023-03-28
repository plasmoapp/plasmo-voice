package su.plo.lib.paper.entity

import lombok.RequiredArgsConstructor
import org.bukkit.Location
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import su.plo.lib.api.entity.MinecraftPlayerEntity
import su.plo.lib.api.server.MinecraftServerLib
import su.plo.lib.api.server.entity.MinecraftServerEntity
import su.plo.lib.api.server.world.ServerPos3d
import su.plo.voice.proto.data.pos.Pos3d

@RequiredArgsConstructor
open class PaperServerEntity<E : LivingEntity>(
    protected val minecraftServer: MinecraftServerLib,
    protected val instance: E
) : MinecraftServerEntity {

    private val position = Pos3d()
    private val lookAngle = Pos3d()

    private var location: Location? = null

    override fun getId() = instance.entityId

    override fun getUUID() = instance.uniqueId

    override fun getPosition() = getPosition(position)

    override fun getPosition(position: Pos3d): Pos3d {
        val location = fetchLocation()

        position.x = location.x
        position.y = location.y
        position.z = location.z

        return position
    }

    override fun getLookAngle() = getLookAngle(lookAngle)

    override fun getLookAngle(lookAngle: Pos3d): Pos3d {
        val vector = instance.location.direction

        lookAngle.x = vector.x
        lookAngle.y = vector.y
        lookAngle.z = vector.z

        return lookAngle
    }

    override fun getEyeHeight() = instance.eyeHeight

    override fun getHitBoxWidth() = instance.boundingBox.widthX.toFloat()

    override fun getHitBoxHeight() = instance.boundingBox.height.toFloat()

    override fun <T> getInstance() = instance as T

    override fun getServerPosition(): ServerPos3d {
        val location = fetchLocation()

        return ServerPos3d(
            minecraftServer.getWorld(instance.world),
            location.x,
            location.y,
            location.z,
            location.yaw,
            location.pitch
        )
    }

    override fun getServerPosition(position: ServerPos3d): ServerPos3d {
        val location = fetchLocation()

        position.world = minecraftServer.getWorld(instance.world)
        position.x = location.x
        position.y = location.y
        position.z = location.z
        position.yaw = location.yaw
        position.pitch = location.pitch

        return position
    }

    override fun getWorld() = minecraftServer.getWorld(instance.world)

    private fun fetchLocation(): Location {
        if (location == null) {
            location = instance.location
        } else {
            instance.getLocation(location)
        }

        return location!!
    }
}
