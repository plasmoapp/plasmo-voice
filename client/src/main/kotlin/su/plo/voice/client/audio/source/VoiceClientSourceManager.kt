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
import su.plo.voice.client.config.ClientConfig
import su.plo.voice.proto.data.audio.source.*
import su.plo.voice.proto.packets.tcp.serverbound.SourceInfoRequestPacket
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit
import java.util.function.Consumer

class VoiceClientSourceManager(
    private val voiceClient: BaseVoiceClient,
    private val config: ClientConfig
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

//    init {
//        voiceClient.backgroundExecutor.scheduleAtFixedRate(
//            { tickSelfSourceInfo() },
//            0L, 5L, TimeUnit.SECONDS
//        )
//    }

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

    override fun getSelfSourceInfos(): Collection<ClientSelfSourceInfo> =
        selfSourceInfoById.values

    override fun clear() {
        sourceById.values.forEach { it.closeAsync().get() }
        sourcesByLineId.clear()
        sourcesByPlayerId.clear()
        sourcesByEntityId.clear()
        sourceRequestById.clear()
        selfSourceInfoById.clear()
    }

    // todo: refactor somehow pepega
    override fun update(sourceInfo: SourceInfo): Unit = runBlocking {
        try {
            if (sourceById.containsKey(sourceInfo.id)) {
                val source = sourceById[sourceInfo.id]!!
                if (source.isClosed()) {
                    sourceRequestById.remove(sourceInfo.id)
                    return@runBlocking
                }
                if (source.sourceInfo.lineId !== sourceInfo.lineId) {
                    sourcesByLineId.remove(source.sourceInfo.lineId, source)
                    sourcesByLineId.put(sourceInfo.lineId, source)
                }

                if (source.sourceInfo.javaClass != sourceInfo.javaClass)
                    return@runBlocking

                when (sourceInfo) {
                    is StaticSourceInfo ->
                        (source as ClientAudioSource<StaticSourceInfo>).update(sourceInfo)

                    is PlayerSourceInfo ->
                        (source as ClientAudioSource<PlayerSourceInfo>).update(sourceInfo)

                    is EntitySourceInfo ->
                        (source as ClientAudioSource<EntitySourceInfo>).update(sourceInfo)

                    is DirectSourceInfo ->
                        (source as ClientAudioSource<DirectSourceInfo>).update(sourceInfo)

                    else -> throw IllegalArgumentException("Invalid source type")
                }
                return@runBlocking
            }

            when (sourceInfo) {
                is PlayerSourceInfo -> {
                    val source = createPlayerSource(sourceInfo)
                    sourceById[sourceInfo.getId()] = source
                    sourcesByLineId.put(sourceInfo.getLineId(), source)
                    sourcesByPlayerId.put(sourceInfo.playerInfo.playerId, source)
                }

                is EntitySourceInfo -> {
                    val source = createEntitySource(sourceInfo)
                    sourceById[sourceInfo.getId()] = source
                    sourcesByLineId.put(sourceInfo.getLineId(), source)
                    sourcesByEntityId.put(sourceInfo.entityId, source)
                }

                is StaticSourceInfo -> {
                    val source = createStaticSource(sourceInfo)
                    sourceById[sourceInfo.getId()] = source
                    sourcesByLineId.put(sourceInfo.getLineId(), source)
                }

                is DirectSourceInfo -> {
                    val source = createDirectSource(sourceInfo)
                    sourceById[sourceInfo.getId()] = source
                    sourcesByLineId.put(sourceInfo.getLineId(), source)
                }

                else -> throw IllegalArgumentException("Invalid source type")
            }
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
            update(selfSourceInfo.sourceInfo)
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

//    private fun tickSelfSourceInfo() {
//        selfSourceInfoById.values
//            .filter {
//                System.currentTimeMillis() - it.lastUpdate > TIMEOUT_MS
//            }
//            .map { it.selfSourceInfo.sourceInfo.id }
//            .forEach { selfSourceInfoById.remove(it) }
//    }

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
        private const val TIMEOUT_MS = 25000L
    }
}
