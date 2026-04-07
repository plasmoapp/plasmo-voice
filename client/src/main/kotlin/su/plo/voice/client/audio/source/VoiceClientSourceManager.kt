package su.plo.voice.client.audio.source

import com.google.common.collect.ListMultimap
import com.google.common.collect.Maps
import com.google.common.collect.Multimaps
import kotlinx.coroutines.runBlocking
import su.plo.voice.api.client.audio.device.DeviceException
import su.plo.voice.api.client.audio.source.ClientAudioSource
import su.plo.voice.api.client.audio.source.ClientSelfSourceInfo
import su.plo.voice.api.client.audio.source.ClientSourceManager
import su.plo.voice.api.client.event.audio.source.AudioSourceClosedEvent
import su.plo.voice.api.event.EventSubscribe
import su.plo.voice.client.BaseVoiceClient
import su.plo.voice.client.config.VoiceClientConfig
import su.plo.voice.proto.data.audio.source.DirectSourceInfo
import su.plo.voice.proto.data.audio.source.EntitySourceInfo
import su.plo.voice.proto.data.audio.source.PlayerSourceInfo
import su.plo.voice.proto.data.audio.source.SelfSourceInfo
import su.plo.voice.proto.data.audio.source.SourceInfo
import su.plo.voice.proto.data.audio.source.StaticSourceInfo
import su.plo.voice.proto.packets.tcp.clientbound.SourceAudioEndPacket
import su.plo.voice.proto.packets.tcp.serverbound.SourceInfoRequestPacket
import su.plo.voice.proto.packets.udp.clientbound.SourceAudioPacket
import java.util.Optional
import java.util.UUID
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit

class VoiceClientSourceManager(
    private val voiceClient: BaseVoiceClient,
    private val config: VoiceClientConfig
) : ClientSourceManager {

    private val sourcesByLineId: ListMultimap<UUID, ClientAudioSource<*>> = Multimaps.newListMultimap(
        Maps.newConcurrentMap(),
        ::CopyOnWriteArrayList
    )

    private val sourcesByPlayerId: ListMultimap<UUID, ClientAudioSource<PlayerSourceInfo>> = Multimaps.newListMultimap(
        Maps.newConcurrentMap(),
        ::CopyOnWriteArrayList
    )

    private val sourcesByEntityId: ListMultimap<Int, ClientAudioSource<EntitySourceInfo>> = Multimaps.newListMultimap(
        Maps.newConcurrentMap(),
        ::CopyOnWriteArrayList
    )

    private val sourceById: MutableMap<UUID, ClientAudioSource<out SourceInfo>> = Maps.newConcurrentMap()
    private val sourceRequestById: MutableMap<UUID, Long> = Maps.newConcurrentMap()
    private val selfSourceInfoById: MutableMap<UUID, VoiceClientSelfSourceInfo> = Maps.newConcurrentMap()
    private val pendingBufferById: MutableMap<UUID, PendingSourceBuffer> = Maps.newConcurrentMap()

    init {
        voiceClient.backgroundExecutor.scheduleAtFixedRate(
            { cleanupStalePendingBuffers() },
            0L, 5L, TimeUnit.SECONDS
        )
    }

    override fun createLoopbackSource(relative: Boolean) =
        ClientLoopbackSource(voiceClient, config, relative)

    override fun getSourceById(sourceId: UUID, request: Boolean): Optional<ClientAudioSource<*>> {
        check(voiceClient.serverConnection.isPresent) { "Not connected" }
        val source = sourceById[sourceId]
        if (source != null) return Optional.of(source)
        if (!request) return Optional.empty()

        // request source
        val lastRequest = sourceRequestById.getOrDefault(sourceId, 0L)
        if (System.currentTimeMillis() - lastRequest > 1000L)
            sendSourceInfoRequest(sourceId)

        return Optional.empty()
    }

    override fun getSourcesByLineId(lineId: UUID): Collection<ClientAudioSource<*>> {
        return sourcesByLineId[lineId]
    }

    override fun getEntitySources(entityId: Int): Collection<ClientAudioSource<EntitySourceInfo>> =
        sourcesByEntityId[entityId]

    override fun getPlayerSources(playerId: UUID): Collection<ClientAudioSource<PlayerSourceInfo>> =
        sourcesByPlayerId[playerId]

    override fun getSourceById(sourceId: UUID) =
        getSourceById(sourceId, true)

    override fun getSources(): Collection<ClientAudioSource<*>> =
        sourceById.values

    override fun getSelfSourceInfo(sourceId: UUID): Optional<ClientSelfSourceInfo> =
        Optional.ofNullable(selfSourceInfoById[sourceId])

    override fun getAllSelfSourceInfos(): Collection<ClientSelfSourceInfo> =
        selfSourceInfoById.values

    override fun clear() {
        sourceById.values.forEach { it.closeAsync().get() }
        sourcesByLineId.clear()
        sourcesByPlayerId.clear()
        sourcesByEntityId.clear()
        sourceRequestById.clear()
        selfSourceInfoById.clear()
        pendingBufferById.clear()
    }

    override fun createOrUpdateSource(sourceInfo: SourceInfo): Unit = runBlocking {
        try {
            if (sourceById.containsKey(sourceInfo.id)) {
                val source = sourceById[sourceInfo.id]!!
                if (source.isClosed()) {
                    sourceRequestById.remove(sourceInfo.id)
                    pendingBufferById.remove(sourceInfo.id)
                    return@runBlocking
                }
                if (source.sourceInfo.lineId !== sourceInfo.lineId) {
                    sourcesByLineId.remove(source.sourceInfo.lineId, source)
                    sourcesByLineId.put(sourceInfo.lineId, source)
                }

                source.updateUnchecked(sourceInfo)
                pendingBufferById.remove(sourceInfo.id)?.drainTo(source)
                return@runBlocking
            }

            val newSource = when (sourceInfo) {
                is PlayerSourceInfo -> createPlayerSource(sourceInfo).apply {
                    sourceById[sourceInfo.getId()] = this
                    sourcesByLineId.put(sourceInfo.getLineId(), this)
                    sourcesByPlayerId.put(sourceInfo.playerInfo.playerId, this)
                }

                is EntitySourceInfo -> createEntitySource(sourceInfo).apply {
                    sourceById[sourceInfo.getId()] = this
                    sourcesByLineId.put(sourceInfo.getLineId(), this)
                    sourcesByEntityId.put(sourceInfo.entityId, this)
                }

                is StaticSourceInfo -> createStaticSource(sourceInfo).apply {
                    sourceById[sourceInfo.getId()] = this
                    sourcesByLineId.put(sourceInfo.getLineId(), this)
                }

                is DirectSourceInfo -> createDirectSource(sourceInfo).apply {
                    sourceById[sourceInfo.getId()] = this
                    sourcesByLineId.put(sourceInfo.getLineId(), this)
                }

                else -> throw IllegalArgumentException("Invalid source type")
            }

            pendingBufferById.remove(sourceInfo.id)?.drainTo(newSource)
            sourceRequestById.remove(sourceInfo.id)
        } catch (e: DeviceException) {
            throw IllegalStateException("Failed to initialize audio source", e)
        }
    }

    override fun sendSourceInfoRequest(sourceId: UUID, requestIfExist: Boolean) {
        if (!requestIfExist && sourceById.containsKey(sourceId)) return

        val connection = voiceClient.serverConnection
            .orElseThrow { IllegalStateException("Not connected") }

        sourceRequestById[sourceId] = System.currentTimeMillis()
        connection.sendPacket(SourceInfoRequestPacket(sourceId))
    }

    override fun updateSelfSourceInfo(selfSourceInfo: SelfSourceInfo) {
        selfSourceInfoById.computeIfAbsent(
            selfSourceInfo.sourceInfo.id
        ) {
            VoiceClientSelfSourceInfo()
        }.selfSourceInfo = selfSourceInfo

        if (getSourceById(selfSourceInfo.sourceInfo.id, false).isPresent) {
            createOrUpdateSource(selfSourceInfo.sourceInfo)
        }
    }

    @EventSubscribe
    fun onAudioSourceClosed(event: AudioSourceClosedEvent) {
        val source = event.source

        voiceClient.eventBus.unregister(voiceClient, source)

        sourceById.remove(source.sourceInfo.id)
        sourcesByLineId.remove(source.sourceInfo.lineId, source)

        (source.sourceInfo as? PlayerSourceInfo)?.playerInfo?.let {
            sourcesByPlayerId.remove(it.playerId, source)
        }

        (source.sourceInfo as? EntitySourceInfo)?.entityId?.let {
            sourcesByEntityId.remove(it, source)
        }
    }

    fun bufferPacket(sourceId: UUID, packet: SourceAudioPacket) {
        val buffer = pendingBufferById.computeIfAbsent(sourceId) {
            PendingSourceBuffer(voiceClient.timeSupplier)
        }

        buffer.offer(packet)

        sourceById[sourceId]?.let { source ->
            buffer.drainTo(source)
            pendingBufferById.remove(sourceId)
        }
    }

    fun bufferPacketIfPending(sourceId: UUID, packet: SourceAudioEndPacket) {
        val buffer = pendingBufferById[sourceId] ?: return

        buffer.offer(packet)

        sourceById[sourceId]?.let { source ->
            buffer.drainTo(source)
            pendingBufferById.remove(sourceId)
        }
    }

    private fun cleanupStalePendingBuffers() {
        val now = voiceClient.timeSupplier.currentTimeMillis
        pendingBufferById.entries.removeIf { (_, buffer) ->
            now - buffer.createdAt > PENDING_BUFFER_TIMEOUT_MS
        }
    }

    private fun createPlayerSource(sourceInfo: PlayerSourceInfo): ClientAudioSource<PlayerSourceInfo> {
        return ClientPlayerSource(
            voiceClient, config, sourceInfo
        ).also { voiceClient.eventBus.register(voiceClient, it) }
    }

    private fun createEntitySource(sourceInfo: EntitySourceInfo): ClientAudioSource<EntitySourceInfo> {
        return ClientEntitySource(
            voiceClient, config, sourceInfo
        ).also { voiceClient.eventBus.register(voiceClient, it) }
    }

    private fun createDirectSource(sourceInfo: DirectSourceInfo): ClientAudioSource<DirectSourceInfo> {
        return ClientDirectSource(
            voiceClient, config, sourceInfo
        ).also { voiceClient.eventBus.register(voiceClient, it) }
    }

    private fun createStaticSource(sourceInfo: StaticSourceInfo): ClientAudioSource<StaticSourceInfo> {
        return ClientStaticSource(
            voiceClient, config, sourceInfo
        ).also { voiceClient.eventBus.register(voiceClient, it) }
    }

    companion object {
        private const val PENDING_BUFFER_TIMEOUT_MS = 5000L
    }
}
