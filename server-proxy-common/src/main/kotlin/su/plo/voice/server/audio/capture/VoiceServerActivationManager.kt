package su.plo.voice.server.audio.capture

import com.google.common.base.Preconditions
import com.google.common.collect.Maps
import com.google.common.collect.Sets
import su.plo.slib.api.permission.PermissionDefault
import su.plo.slib.api.permission.PermissionTristate
import su.plo.voice.api.addon.AddonContainer
import su.plo.voice.api.event.EventPriority
import su.plo.voice.api.event.EventSubscribe
import su.plo.voice.api.server.PlasmoBaseVoiceServer
import su.plo.voice.api.server.audio.capture.ServerActivation
import su.plo.voice.api.server.audio.capture.ServerActivationManager
import su.plo.voice.api.server.connection.PacketManager
import su.plo.voice.api.server.event.audio.capture.ServerActivationRegisterEvent
import su.plo.voice.api.server.event.audio.capture.ServerActivationUnregisterEvent
import su.plo.voice.api.server.event.audio.source.PlayerSpeakEndEvent
import su.plo.voice.api.server.event.audio.source.PlayerSpeakEvent
import su.plo.voice.api.server.event.player.PlayerPermissionUpdateEvent
import su.plo.voice.api.server.player.VoicePlayer
import su.plo.voice.proto.data.audio.capture.VoiceActivation
import su.plo.voice.proto.data.audio.codec.CodecInfo
import su.plo.voice.proto.packets.tcp.clientbound.ActivationRegisterPacket
import su.plo.voice.proto.packets.tcp.clientbound.ActivationUnregisterPacket
import su.plo.voice.proto.packets.tcp.clientbound.ClientPacketTcpHandler
import su.plo.voice.proto.packets.tcp.serverbound.PlayerAudioEndPacket
import su.plo.voice.proto.packets.udp.serverbound.PlayerAudioPacket
import su.plo.voice.server.player.BaseVoicePlayer
import java.util.*
import java.util.function.Consumer

class VoiceServerActivationManager(
    private val voiceServer: PlasmoBaseVoiceServer,
    private val tcpConnections: PacketManager<ClientPacketTcpHandler, out VoicePlayer>,
    private val weightSupplier: ((activationName: String) -> Optional<Int>)?
) : ServerActivationManager {

    private val activationById: MutableMap<UUID, ServerActivation> = Maps.newConcurrentMap()
    private val selfActivationHelper = SelfActivationHelper(voiceServer)

    init {
        voiceServer.eventBus.register(voiceServer, selfActivationHelper)
    }

    override fun getActivationById(id: UUID) =
        Optional.ofNullable(activationById[id])

    override fun getActivationByName(name: String) =
        Optional.ofNullable(activationById[VoiceActivation.generateId(name)])

    override fun getActivations() =
        activationById.values

    override fun createBuilder(
        addonObject: Any,
        name: String,
        translation: String,
        icon: String,
        permission: String,
        weight: Int
    ): ServerActivation.Builder {
        val addon = voiceServer.addonManager.getAddon(addonObject)
            .orElseThrow { IllegalArgumentException("addonObject is not an addon") }

        if (activationById.containsKey(VoiceActivation.generateId(name))) {
            throw IllegalArgumentException("Activation with name $name already exists")
        }

        return VoiceServerActivationBuilder(
            addon,
            name,
            translation,
            icon,
            Sets.newHashSet<String>(permission),
            weightSupplier?.invoke(name)?.orElse(null) ?: weight
        )
    }

    override fun unregister(id: UUID): Boolean {
        val activation = activationById[id] ?: return false

        ServerActivationUnregisterEvent(activation).also { event ->
            if (!voiceServer.eventBus.fire(event)) return false
        }

        activation.permissions.forEach { permission ->
            voiceServer.minecraftServer
                .permissionManager
                .unregister(permission)
        }

        activationById.remove(id)

        voiceServer.playerManager.players
            .filter { it.hasVoiceChat() }
            .forEach {
                val player = it as BaseVoicePlayer<*>
                player.removeActivationDistance(activation)
                player.activeActivations.remove(activation)
            }

        tcpConnections.broadcast(
            ActivationUnregisterPacket(activation.id)
        ) { activation.checkPermissions(it) }

        return true
    }

    override fun clear() {
        activationById.values.forEach(this::unregister)
        activationById.clear()
    }

    @EventSubscribe(priority = EventPriority.LOW)
    fun onPlayerSpeak(event: PlayerSpeakEvent) {
        val player = event.player as BaseVoicePlayer<*>
        val originalPacket = event.packet

        val activation = getActivationById(originalPacket.activationId)
            .orElse(null) as VoiceServerActivation?
            ?: return

        if (!activation.checkPermissions(player)) return

        val distance = activation.calculateAllowedDistance(originalPacket.distance.toInt()).toShort()
        val packet = PlayerAudioPacket(
            originalPacket.sequenceNumber,
            originalPacket.data,
            originalPacket.activationId,
            distance,
            originalPacket.isStereo
        )

        if (activation.requirements?.checkRequirements(player, originalPacket) == false) return

        val lastActivationSequenceNumber = player.lastActivationSequenceNumber.getOrDefault(activation.id, 0)
        if (activation !in player.activeActivations &&
            packet.sequenceNumber > lastActivationSequenceNumber
        ) {
            player.activeActivations.add(activation)
            activation.activationStartListeners.forEach { it.onActivationStart(player) }
        }

        for (listener in activation.activationListeners) {
            val result = listener.onActivation(player, packet)
            if (result == ServerActivation.Result.HANDLED) {
                event.result = result
                event.isCancelled = true
                break
            }
        }
    }

    @EventSubscribe(priority = EventPriority.LOW)
    fun onPlayerSpeakEnd(event: PlayerSpeakEndEvent) {
        val player = event.player as BaseVoicePlayer<*>
        val originalPacket = event.packet

        val activation = getActivationById(originalPacket.activationId)
            .orElse(null) as VoiceServerActivation?
            ?: return

        if (!player.activeActivations.contains(activation)) return
        if (!activation.checkPermissions(player)) return

        val distance = activation.calculateAllowedDistance(originalPacket.distance.toInt()).toShort()
        val packet = PlayerAudioEndPacket(
            originalPacket.sequenceNumber,
            originalPacket.activationId,
            distance
        )

        if (!activation.checkDistance(packet.distance.toInt())) return
        if (activation.requirements?.checkRequirements(player, packet) == false) return

        player.activeActivations.remove(activation)
        player.lastActivationSequenceNumber[activation.id] = packet.sequenceNumber

        for (listener in activation.activationEndListeners) {
            val result = listener.onActivationEnd(player, packet)
            if (result == ServerActivation.Result.HANDLED) {
                event.result = result
                event.isCancelled = true
                break
            }
        }
    }

    @EventSubscribe
    fun onPermissionUpdate(event: PlayerPermissionUpdateEvent) {
        val player = event.player
        val permission = event.permission

        if (permission == WILDCARD_ACTIVATION_PERMISSION) {
            when (player.instance.getPermission(WILDCARD_ACTIVATION_PERMISSION)) {
                PermissionTristate.TRUE -> activationById.values.forEach {
                    player.sendPacket(ActivationRegisterPacket(it as VoiceActivation))
                }

                PermissionTristate.FALSE -> activationById.keys.forEach {
                    player.sendPacket(ActivationUnregisterPacket(it))
                }

                PermissionTristate.UNDEFINED -> activationById.forEach { (activationId, activation) ->
                    if (activation.checkPermissions(player)) {
                        player.sendPacket(ActivationRegisterPacket(activation as VoiceActivation))
                    } else {
                        player.sendPacket(ActivationUnregisterPacket(activationId))
                    }
                }
            }
            return
        }

        if (activationById.values.none { it.permissions.contains(permission) }) return

        val permissionSplit = permission.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        val activation = getActivationByName(permissionSplit[permissionSplit.size - 1]).orElse(null) ?: return

        if (activation.checkPermissions(player)) {
            player.sendPacket(ActivationRegisterPacket(activation as VoiceActivation))
        } else {
            player.sendPacket(ActivationUnregisterPacket(activation.id))
        }
    }

    inner class VoiceServerActivationBuilder(
        val addon: AddonContainer,
        val name: String,
        val translation: String,
        val icon: String,
        val permissions: MutableSet<String>,
        val weight: Int
    ) : ServerActivation.Builder {

        private var distances = emptyList<Int>()
        private var defaultDistance = 0
        private var transitive = true
        private var proximity = true
        private var stereoSupported = false
        private var permissionDefault: PermissionDefault? = null
        private var encoderInfo: CodecInfo? = null
        private var requirements: ServerActivation.Requirements? = null

        override fun addPermission(permission: String) = apply {
            permissions.add(permission)
        }

        override fun setPermissionDefault(permissionDefault: PermissionDefault?) = apply {
            this.permissionDefault = permissionDefault
        }

        override fun setRequirements(requirements: ServerActivation.Requirements?) = apply {
            this.requirements = requirements
        }

        override fun setDistances(distances: List<Int>) = apply {
            this.distances = Preconditions.checkNotNull(distances)
        }

        override fun setDefaultDistance(defaultDistance: Int) = apply {
            this.defaultDistance = defaultDistance
        }

        override fun setTransitive(transitive: Boolean) = apply {
            this.transitive = transitive
        }

        override fun setProximity(proximity: Boolean) = apply {
            this.proximity = proximity
        }

        override fun setStereoSupported(stereoSupported: Boolean) = apply {
            this.stereoSupported = stereoSupported
        }

        override fun setEncoderInfo(encoderInfo: CodecInfo?) = apply {
            this.encoderInfo = encoderInfo
        }

        override fun build(): ServerActivation {
            check(activationById[VoiceActivation.generateId(name)] == null)
            { "Activation with name $name already exists" }

            val activation = VoiceServerActivation(
                addon,
                name,
                translation,
                icon,
                permissions,
                distances,
                defaultDistance,
                proximity,
                transitive,
                stereoSupported,
                encoderInfo,
                weight,
                requirements
            )

            if (permissionDefault != null) {
                permissions.forEach(
                    Consumer { permission: String? ->
                        voiceServer.minecraftServer
                            .permissionManager
                            .register(permission!!, permissionDefault!!)
                    }
                )
            }

            ServerActivationRegisterEvent(activation).also { event ->
                check(voiceServer.eventBus.fire(event)) { "Activation registration was cancelled" }
            }

            activationById[activation.id] = activation

            tcpConnections.broadcast(
                ActivationRegisterPacket(activation)
            ) { activation.checkPermissions(it) }

            return activation
        }
    }

    companion object {
        private const val WILDCARD_ACTIVATION_PERMISSION = "pv.activation.*"
    }
}
