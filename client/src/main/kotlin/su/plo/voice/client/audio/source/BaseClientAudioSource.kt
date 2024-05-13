package su.plo.voice.client.audio.source

import kotlinx.coroutines.*
import kotlinx.coroutines.future.future
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.minecraft.client.Minecraft
import net.minecraft.client.player.LocalPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.lwjgl.openal.AL10
import su.plo.config.entry.BooleanConfigEntry
import su.plo.config.entry.DoubleConfigEntry
import su.plo.lib.mod.extensions.eyePosition
import su.plo.voice.BaseVoice
import su.plo.voice.api.audio.codec.AudioDecoder
import su.plo.voice.api.audio.codec.CodecException
import su.plo.voice.api.client.audio.device.DeviceException
import su.plo.voice.api.client.audio.device.source.AlSource
import su.plo.voice.api.client.audio.device.source.AlSourceParams
import su.plo.voice.api.client.audio.source.ClientAudioSource
import su.plo.voice.api.client.time.TimeSupplier
import su.plo.voice.api.client.connection.ServerInfo.VoiceInfo
import su.plo.voice.api.client.event.audio.device.source.AlSourceClosedEvent
import su.plo.voice.api.client.event.audio.device.source.AlStreamSourceStoppedEvent
import su.plo.voice.api.client.event.audio.source.AudioSourceClosedEvent
import su.plo.voice.api.client.event.audio.source.AudioSourceInitializedEvent
import su.plo.voice.api.client.event.audio.source.AudioSourceResetEvent
import su.plo.voice.api.encryption.Encryption
import su.plo.voice.api.encryption.EncryptionException
import su.plo.voice.api.event.EventPriority
import su.plo.voice.api.event.EventSubscribe
import su.plo.voice.api.util.AudioUtil
import su.plo.voice.audio.codec.AudioDecoderPlc
import su.plo.voice.client.BaseVoiceClient
import su.plo.voice.client.audio.SoundOcclusion
import su.plo.voice.client.config.VoiceClientConfig
import su.plo.voice.client.extension.diff
import su.plo.voice.client.extension.level
import su.plo.voice.client.extension.toFloatArray
import su.plo.voice.proto.data.audio.codec.CodecInfo
import su.plo.voice.proto.data.audio.source.SourceInfo
import su.plo.voice.proto.packets.tcp.clientbound.SourceAudioEndPacket
import su.plo.voice.proto.packets.udp.clientbound.SourceAudioPacket
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.acos
import kotlin.math.pow

abstract class BaseClientAudioSource<T>(
    protected val voiceClient: BaseVoiceClient,
    protected val config: VoiceClientConfig,
    final override var sourceInfo: T
) : ClientAudioSource<T> where T : SourceInfo {

    private val timeSupplier: TimeSupplier
        get() = voiceClient.timeSupplier

    override var source: AlSource = runBlocking { createSource(sourceInfo) }

    private var lineVolume: DoubleConfigEntry
    private var lineMute: BooleanConfigEntry
    open var sourceVolume: DoubleConfigEntry = config.voice
        .volumes
        .getVolume("source_${sourceInfo.id}")

    private var encryption: Encryption? = null
    private var decoder: AudioDecoder? = null

    private var endRequest: Job? = null

    override var closeTimeoutMs: Long = 500
        set(value) {
            source.setCloseTimeoutMs(value)
            field = value
        }

    private var lastSequenceNumbers: MutableMap<UUID, Long> = HashMap()
    private var lastActivation = 0L
    private var lastOcclusion = -1.0

    private val closed = AtomicBoolean(false)
    private val resetted = AtomicBoolean(false)
    private val activated = AtomicBoolean(false)
    private val canHear = AtomicBoolean(false)

    private val mutex = Mutex()

    init {
        val serverInfo = voiceClient.serverInfo
            .orElseThrow { IllegalStateException("Not connected") }
        val voiceInfo = serverInfo.voiceInfo

        // initialize decoder
        sourceInfo.decoderInfo?.let {
            decoder = createDecoder(sourceInfo, voiceInfo, it)
        }

        // initialize encryption
        serverInfo.encryption.ifPresent {
            encryption = it
        }

        // initialize volumes
        lineVolume = getLineVolume(sourceInfo)
        lineMute = getLineMute(sourceInfo)
        BaseVoice.DEBUG_LOGGER.log(
            "Source {} initialized in {}",
            sourceInfo,
            if (isStereo(sourceInfo)) "stereo" else "mono"
        )

        voiceClient.eventBus.fire(AudioSourceInitializedEvent(this))
    }

    override fun update(sourceInfo: T): Unit = runBlocking {
        mutex.withLock {
            val serverInfo = voiceClient.serverInfo
                .orElseThrow { IllegalStateException("Not connected") }

            val voiceInfo = serverInfo.voiceInfo
            val stereoChanged = isStereo(this@BaseClientAudioSource.sourceInfo) != isStereo(sourceInfo)

            // initialize sources
            if (stereoChanged) {
                val oldSource = source
                source = createSource(sourceInfo)
                oldSource.closeAsync()

                BaseVoice.DEBUG_LOGGER.log(
                    "Update device sources for {} in {}",
                    sourceInfo,
                    if (isStereo(sourceInfo)) "stereo" else "mono"
                )
            }

            // initialize decoder
            if (sourceInfo.isStereo != this@BaseClientAudioSource.sourceInfo.isStereo) {
                decoder?.close()

                sourceInfo.decoderInfo?.let {
                    decoder = createDecoder(sourceInfo, voiceInfo, it)
                }
                lastSequenceNumbers.clear()
                BaseVoice.DEBUG_LOGGER.log("Update decoder for {}", sourceInfo)
            }

            // initialize encryption
            serverInfo.encryption.ifPresent {
                encryption = it
            }

            // initialize volumes
            if (sourceInfo.lineId != this@BaseClientAudioSource.sourceInfo.lineId) {
                lineVolume = getLineVolume(sourceInfo)
                lineMute = getLineMute(sourceInfo)
                BaseVoice.DEBUG_LOGGER.log("Update source line for {}", sourceInfo)
            }

            this@BaseClientAudioSource.sourceInfo = sourceInfo

            voiceClient.eventBus.fire(AudioSourceInitializedEvent(this@BaseClientAudioSource))
        }
    }

    override fun process(packet: SourceAudioPacket) {
        if (isClosed() || lineMute.value()) return

        SCOPE.launch { processAudioPacket(packet) }
    }

    override fun process(packet: SourceAudioEndPacket) {
        if (isClosed() || lineMute.value()) return

        SCOPE.launch { processAudioEndPacket(packet) }
        endRequest?.cancel()

        // because SourceAudioEndPacket can be received BEFORE the end of the stream,
        // we need to wait for some time to actually end the stream
        endRequest = SCOPE.launch {
            try {
                delay(100L)
                reset(AudioSourceResetEvent.Cause.VOICE_END)
            } catch (_: CancellationException) {
            }
        }
    }

    override suspend fun close() = mutex.withLock {
        activated.set(false)
        canHear.set(false)
        closed.set(true)

        decoder?.close()
        source.closeAsync()

        voiceClient.eventBus.fire(AudioSourceClosedEvent(this@BaseClientAudioSource))
        BaseVoice.DEBUG_LOGGER.log("Source {} closed", sourceInfo)
    }

    override fun closeAsync(): CompletableFuture<Void?> =
        SCOPE.future {
            close()
            null
        }

    override fun isActivated(): Boolean {
        if (activated.get()) {
            if (closeTimeoutMs > 0L && timeSupplier.currentTimeMillis - lastActivation > closeTimeoutMs) {
                resetAsync(AudioSourceResetEvent.Cause.TIMED_OUT)
            }

            return true
        }

        return false
    }

    override fun isClosed(): Boolean {
        return closed.get()
    }

    override fun canHear(): Boolean {
        return canHear.get()
    }

    @EventSubscribe(priority = EventPriority.LOWEST)
    fun onSourceClosed(event: AlSourceClosedEvent) {
        if (closed.get() || source != event.source) return
        closeAsync()
    }

    @EventSubscribe(priority = EventPriority.LOWEST)
    fun onSourceStopped(event: AlStreamSourceStoppedEvent) {
        if (closed.get() || source != event.source) return
        resetAsync(AudioSourceResetEvent.Cause.SOURCE_STOPPED)
    }

    private suspend fun processAudioPacket(packet: SourceAudioPacket) = mutex.withLock {
        // drop packets if source state diff by more than 10
        if (sourceInfo.state.diff(packet.sourceState) >= 10) {
            BaseVoice.DEBUG_LOGGER.warn("Drop packet with bad source state {}", sourceInfo)
            return
        }

        val lastSequenceNumber = lastSequenceNumbers[sourceInfo.lineId] ?: -1L

        // drop packet with bad order
        if (lastSequenceNumber >= 0 && packet.sequenceNumber <= lastSequenceNumber) {
            if (lastSequenceNumber - packet.sequenceNumber < 10L) {
                BaseVoice.DEBUG_LOGGER.log("Drop packet with bad order")
                return
            }
            lastSequenceNumbers.remove(sourceInfo.lineId)
        }

        endRequest?.let {
            it.cancel()
            endRequest = null
        }

        // get source positions
        val playerPosition = getListenerPosition()
        val position = getPosition()
        val lookAngle = getLookAngle()

        val distance = packet.distance.toDouble()

        val sourceDistance = position.distanceTo(playerPosition)
        val distanceGain = calculateDistanceGain(sourceDistance.coerceAtMost(distance), distance)

        // calculate volume
        var volume = config.voice.volume.value() * sourceVolume.value() * lineVolume.value()
        if (shouldCalculateOcclusion()) {
            var occlusion: Double = calculateOcclusion(position)
            if (lastOcclusion >= 0) {
                lastOcclusion = if (occlusion > lastOcclusion) {
                    (lastOcclusion + 0.05).coerceAtLeast(0.0)
                } else {
                    (lastOcclusion - 0.05).coerceAtLeast(occlusion)
                }
                occlusion = lastOcclusion
            }

            volume *= (1.0 - occlusion)
            if (lastOcclusion == -1.0) {
                lastOcclusion = occlusion
            }
        }

        if (config.advanced.exponentialVolumeSlider.value() && volume < 1) {
            volume = volume.pow(3)
        }

        // calculate and apply directional angle gain
        if (shouldCalculateDirectionalGain()) {
            val positionDiffNormalized = playerPosition.subtract(position).normalize()
            val angle = Math.toDegrees(acos(positionDiffNormalized.dot(lookAngle)))

            val innerAngle =
                if (sourceInfo.angle > 0) sourceInfo.angle / 2
                else config.advanced.directionalSourcesAngle.value() / 2

            if (angle > innerAngle) {
                val outAngle = angle - innerAngle
                volume *= calculateAngleGain(outAngle, innerAngle.toDouble())
            }
        }

        volume *= distanceGain

        // update source volume & distance
        updateSource(volume.toFloat(), position)

        // after updating the source, source can be closed by reloading the device,
        // so we need to make sure that source is not closed rn
        if (closed.get()) return

        // packet compensation
        if (lastSequenceNumber >= 0) {
            val packetsToCompensate = (packet.sequenceNumber - (lastSequenceNumber + 1)).toInt()
            if (packetsToCompensate in 1..4) {
                BaseVoice.DEBUG_LOGGER.warn("Compensate {} lost packets", packetsToCompensate)
                for (i in 0 until packetsToCompensate) {
                    if (decoder != null && decoder is AudioDecoderPlc && !sourceInfo.isStereo) {
                        try {
                            write((decoder as AudioDecoderPlc).decodePLC())
                        } catch (e: CodecException) {
                            LOGGER.warn("Failed to decode source audio", e)
                            return
                        }
                    } else {
                        write(ShortArray(0))
                    }
                }
            }
        }

        // decrypt & decode samples
        try {
            val decrypted = encryption?.decrypt(packet.data) ?: packet.data
            val decoded = decoder?.decode(decrypted) ?: AudioUtil.bytesToShorts(decrypted)

            if (sourceInfo.isStereo && config.advanced.stereoSourcesToMono.value()) {
                write(AudioUtil.convertToMonoShorts(decoded))
            } else {
                write(decoded)
            }
        } catch (e: EncryptionException) {
            BaseVoice.DEBUG_LOGGER.warn("Failed to decrypt source audio", e)
        } catch (e: CodecException) {
            BaseVoice.DEBUG_LOGGER.warn("Failed to decode source audio", e)
        }

        lastSequenceNumbers[sourceInfo.lineId] = packet.sequenceNumber
        lastActivation = timeSupplier.currentTimeMillis

        if (distance > 0) canHear.set(sourceDistance <= distance)
        activated.set(true)
        resetted.set(false)
    }

    private suspend fun processAudioEndPacket(packet: SourceAudioEndPacket) = mutex.withLock {
        if (!activated.get()) return
        lastSequenceNumbers[sourceInfo.lineId] = packet.sequenceNumber
    }

    private suspend fun reset(cause: AudioSourceResetEvent.Cause) = mutex.withLock {
        val event = AudioSourceResetEvent(this, cause)
        if (!voiceClient.eventBus.fire(event)) return@withLock

        if (!resetted.compareAndSet(false, true)) return
        if (decoder != null) decoder!!.reset()
        activated.set(false)
        canHear.set(false)

        if (cause == AudioSourceResetEvent.Cause.TIMED_OUT) {
            LOGGER.debug("Voice end packet was not received")
        }
    }

    private fun resetAsync(cause: AudioSourceResetEvent.Cause) =
        SCOPE.future {
            reset(cause)
            null
        }

    protected fun getListener(): Entity? =
        if (config.advanced.cameraSoundListener.value()
            && voiceClient.serverInfo.orElse(null)
                ?.playerInfo
                ?.get("pv.allow_freecam")
                ?.orElse(true) == true
        ) Minecraft.getInstance().cameraEntity
        else Minecraft.getInstance().player

    protected open fun getListenerPosition(): Vec3 =
        getListener()?.eyePosition() ?: Vec3.ZERO

    protected open fun calculateAngleGain(outAngle: Double, innerAngle: Double): Double {
        var angleGain = 1.0 - outAngle / (OUTER_ANGLE - innerAngle)
        if (config.advanced.exponentialDistanceGain.value())
            angleGain = angleGain.pow(3.0)

        return angleGain
    }

    protected open fun calculateDistanceGain(sourceDistance: Double, maxDistance: Double): Double {
        var distanceGain = 1.0 - (sourceDistance / maxDistance)
        if (config.advanced.exponentialDistanceGain.value())
            distanceGain = distanceGain.pow(3.0)

        return distanceGain
    }

    protected abstract fun getPosition(): Vec3

    protected abstract fun getLookAngle(): Vec3

    protected open fun shouldCalculateDirectionalGain(): Boolean =
        config.voice.directionalSources.value() || sourceInfo.angle > 0

    protected open fun shouldCalculateOcclusion(): Boolean {
        return !config.voice.soundOcclusion.isDisabled && config.voice.soundOcclusion.value()
    }

    private fun calculateOcclusion(position: Vec3): Double {
        val player: LocalPlayer = Minecraft.getInstance().player ?: return 0.0

        return SoundOcclusion.getOccludedPercent(
            player.level(),
            position,
            player.eyePosition()
        )
    }

    private fun write(samples: ShortArray) {
        source.write(samples)
    }

    private suspend fun updateSource(volume: Float, position: Vec3) {
        source.device.runInContext {
            source.volume = volume

            if (isPanningDisabled()) {
                source.setInt(
                    0x202,  // AL_SOURCE_RELATIVE
                    1
                )
                source.setFloatArray(0x1004, POSITION_ZERO)
                return@runInContext
            } else {
                source.setInt(
                    0x202,  // AL_SOURCE_RELATIVE
                    0
                )
            }

            source.setFloatArray(0x1004, position.toFloatArray()) // AL_POSITION
        }
    }

    private suspend fun createSource(sourceInfo: T): AlSource {
        val device = voiceClient.deviceManager.outputDevice.orElseThrow { DeviceException("Output device is null") }
        val source = device.createSource(isStereo(sourceInfo), AlSourceParams.DEFAULT)

        device.runInContext {
            source.setFloat(0x100E, 4f) // AL_MAX_GAIN
            source.setInt(AL10.AL_DISTANCE_MODEL, AL10.AL_NONE)
            source.play()
        }

        return source
    }

    private fun createDecoder(sourceInfo: T, voiceInfo: VoiceInfo, decoderInfo: CodecInfo): AudioDecoder {
        return voiceClient.codecManager.createDecoder(
            decoderInfo,
            voiceInfo.captureInfo.sampleRate,
            sourceInfo.isStereo,
            voiceInfo.frameSize
        )
    }

    private fun getLineVolume(sourceInfo: T): DoubleConfigEntry {
        val sourceLine = voiceClient.sourceLineManager.getLineById(sourceInfo.lineId)
            .orElseThrow { IllegalStateException("Source line not found") }

        return config.voice
            .volumes
            .getVolume(sourceLine.name)
    }

    private fun getLineMute(sourceInfo: T): BooleanConfigEntry {
        val sourceLine = voiceClient.sourceLineManager.getLineById(sourceInfo.lineId)
            .orElseThrow { IllegalStateException("Source line not found") }

        return config.voice
            .volumes
            .getMute(sourceLine.name)
    }

    protected open fun isPanningDisabled(): Boolean =
        !config.advanced.panning.value()

    private fun isStereoOrPanningDisabled(sourceInfo: SourceInfo): Boolean =
        isPanningDisabled() || isStereo(sourceInfo)

    private fun isStereo(sourceInfo: SourceInfo): Boolean {
        return sourceInfo.isStereo && !config.advanced.stereoSourcesToMono.value()
    }

    companion object {
        private val OUTER_ANGLE: Double = 180.0
        private val LOGGER: Logger = LogManager.getLogger(BaseClientAudioSource::class.java)
        private val POSITION_ZERO = floatArrayOf(0f, 0f, 0f)

        private val SCOPE = CoroutineScope(Executors.newSingleThreadExecutor().asCoroutineDispatcher())
    }
}
