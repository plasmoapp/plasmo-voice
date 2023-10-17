package su.plo.voice.server.audio.capture

import com.google.common.base.Preconditions
import com.google.common.collect.Sets
import su.plo.slib.api.entity.player.McPlayer
import su.plo.voice.api.addon.AddonContainer
import su.plo.voice.api.server.audio.capture.ServerActivation
import su.plo.voice.api.server.audio.capture.ServerActivation.Requirements
import su.plo.voice.proto.data.audio.capture.VoiceActivation
import su.plo.voice.proto.data.audio.codec.CodecInfo
import java.util.*

class VoiceServerActivation(
    private val addon: AddonContainer,
    name: String,
    translation: String,
    icon: String,
    permissions: MutableSet<String>,
    distances: List<Int>,
    defaultDistance: Int,
    proximity: Boolean,
    transitive: Boolean,
    stereoSupported: Boolean,
    encoderInfo: CodecInfo?,
    weight: Int,
    val requirements: Requirements?
) : VoiceActivation(
    name,
    translation,
    icon,
    distances,
    defaultDistance,
    proximity,
    stereoSupported,
    transitive,
    encoderInfo,
    weight
), ServerActivation {

    val permissions: MutableSet<String>

    val activationListeners: MutableSet<ServerActivation.PlayerActivationListener> =
        Sets.newConcurrentHashSet()
    val activationStartListeners: MutableSet<ServerActivation.PlayerActivationStartListener> =
        Sets.newConcurrentHashSet()
    val activationEndListeners: MutableSet<ServerActivation.PlayerActivationEndListener> =
        Sets.newConcurrentHashSet()

    init {
        this.transitive = transitive
        this.permissions = permissions
    }

    override fun addPermission(permission: String) {
        permissions.add(permission)
    }

    override fun removePermission(permission: String) {
        permissions.remove(permission)
    }

    override fun clearPermissions() =
        permissions.clear()

    override fun checkPermissions(serverPlayer: McPlayer) =
        permissions.any { serverPlayer.hasPermission(it) }

    override fun setDistances(distances: List<Int>) {
        this.distances = Preconditions.checkNotNull(distances)
    }

    override fun checkDistance(distance: Int): Boolean {
        if (distances.size == 0)
            return true

        if (distances.size == 2 && distances[0] == -1)
            return distance in 1..distances[1]

        return distances.contains(distance)
    }

    override fun setTransitive(transitive: Boolean) {
        this.transitive = transitive
    }

    override fun setProximity(proximity: Boolean) {
        this.proximity = proximity
    }

    override fun getAddon(): AddonContainer = addon

    override fun getRequirements(): Optional<Requirements> =
        Optional.ofNullable(requirements)

    override fun getPermissions(): Collection<String> = permissions

    override fun onPlayerActivation(activationListener: ServerActivation.PlayerActivationListener) {
        activationListeners.add(activationListener)
    }

    override fun onPlayerActivationStart(activationStartListener: ServerActivation.PlayerActivationStartListener) {
        activationStartListeners.add(activationStartListener)
    }

    override fun onPlayerActivationEnd(activationEndListener: ServerActivation.PlayerActivationEndListener) {
        activationEndListeners.add(activationEndListener)
    }

    override fun equals(o: Any?): Boolean {
        return o === this || super.equals(o)
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }
}
