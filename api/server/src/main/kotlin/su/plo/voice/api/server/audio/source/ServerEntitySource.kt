package su.plo.voice.api.server.audio.source

import su.plo.slib.api.server.entity.McServerEntity
import su.plo.voice.proto.data.audio.source.EntitySourceInfo

/**
 * Represents an entity audio source.
 */
interface ServerEntitySource : ServerProximitySource<EntitySourceInfo> {

    /**
     * Gets the entity associated with this audio source.
     *
     * @return The entity.
     */
    val entity: McServerEntity
}
