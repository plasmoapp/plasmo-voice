package su.plo.voice.client.audio.device

import com.google.common.base.Preconditions
import com.google.common.collect.Sets
import kotlinx.coroutines.*
import kotlinx.coroutines.future.future
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.minecraft.client.Minecraft
import net.minecraft.world.phys.Vec3
import org.apache.logging.log4j.LogManager
import org.joml.Quaternionf
import org.joml.Vector3f
import org.lwjgl.BufferUtils
import org.lwjgl.openal.*
import su.plo.lib.mod.extensions.eyePosition
import su.plo.voice.api.client.PlasmoVoiceClient
import su.plo.voice.api.client.audio.device.*
import su.plo.voice.api.client.audio.device.source.AlSource
import su.plo.voice.api.client.audio.device.source.AlSourceParams
import su.plo.voice.api.client.audio.device.source.DeviceSourceParams
import su.plo.voice.api.client.event.audio.device.DeviceClosedEvent
import su.plo.voice.api.client.event.audio.device.DeviceOpenEvent
import su.plo.voice.api.client.event.audio.device.DevicePreOpenEvent
import su.plo.voice.api.client.event.audio.device.source.AlSourceClosedEvent
import su.plo.voice.api.event.EventPriority
import su.plo.voice.api.event.EventSubscribe
import su.plo.voice.client.audio.AlUtil
import su.plo.voice.client.audio.device.source.StreamAlSource.Companion.create
import java.nio.Buffer
import java.nio.IntBuffer
import java.util.*
import java.util.concurrent.*
import javax.sound.sampled.AudioFormat

class AlOutputDevice
@Throws(DeviceException::class) constructor(
    voiceClient: PlasmoVoiceClient,
    name: String,
    format: AudioFormat
) :
    BaseAudioDevice(voiceClient, name, format),
    AlContextOutputDevice {

    val coroutineScope: CoroutineScope

    private val listener: AlListener = AlListener()

    private val sources: MutableSet<AlSource> = Sets.newHashSet()
    private var hrtfSupported = false

    override var devicePointer: Long = 0
    override var contextPointer: Long = 0

    private var hasDisconnectEXT = false
    private var disconnected = false

    private val mutex = Mutex()

    init {
        coroutineScope = CoroutineScope(Executors.newSingleThreadScheduledExecutor { r ->
            val thread = Thread(
                null,
                r,
                "Al Output Device $name",
                0
            )
            if (thread.isDaemon) thread.isDaemon = false
            if (thread.priority != Thread.NORM_PRIORITY) thread.priority = Thread.NORM_PRIORITY
            thread
        }.asCoroutineDispatcher())

        open()
    }

    override fun reload() {
        if (!isOpen()) return

        runBlocking(coroutineScope.coroutineContext) {
            mutex.withLock {
                closeSync()
                openSync()
            }
        }
    }

    override fun close() {
        if (!isOpen() && !disconnected) return

        runBlocking(coroutineScope.coroutineContext) {
            mutex.withLock {
                closeSync()
            }
        }
    }

    override fun isOpen(): Boolean {
        if (devicePointer != 0L && hasDisconnectEXT && ALC11.alcGetInteger(devicePointer, 787) == 0 && !disconnected) {
            disconnected = true
        }

        return devicePointer != 0L && !disconnected
    }

    @Throws(DeviceException::class)
    override fun createSource(stereo: Boolean, params: DeviceSourceParams): AlSource {
        Preconditions.checkNotNull(params, "params cannot be null")
        if (!isOpen()) throw DeviceException("Device is not open")

        val numBuffers = (params as? AlSourceParams)?.numBuffers ?: 0
        if (numBuffers > 0) {
            if (numBuffers < 4) {
                throw DeviceException("Min number of buffers is 4")
            } else if (numBuffers > 64) {
                throw DeviceException("Max number of buffers is 64")
            }
        }

        return try {
            runBlocking(coroutineScope.coroutineContext) {
                mutex.withLock {
                    val source = create(this@AlOutputDevice, voiceClient, stereo, numBuffers)
                    sources.add(source)
                    source
                }
            }
        } catch (e: RuntimeException) {
            if (e.cause is DeviceException) {
                throw (e.cause as DeviceException?)!!
            }
            throw DeviceException("Failed to allocate new source", e)
        }
    }

    override suspend fun closeSources() {
        Sets.newHashSet(sources).forEach {
            it.close()
        }
    }

    override fun closeSourcesAsync(): CompletableFuture<Void?> =
        coroutineScope.future {
            closeSources()
            null
        }

    override suspend fun runInContext(runnable: suspend () -> Unit) {
        coroutineScope.launch { runnable() }.join()
//        try {
////            if (AlUtil.sameDeviceContext(this)) {
////                runnable()
////                return
////            }
//
//
//        } catch (e: Exception) {
//            throw RuntimeException(e)
//        }
    }

    override fun runInContextAsync(runnable: Runnable): CompletableFuture<Void?> =
        coroutineScope.future {
            runnable.run()
            null
        }

    override fun open(): Unit = runBlocking(coroutineScope.coroutineContext) {
        mutex.withLock {
            openSync()
        }
    }

    @EventSubscribe(priority = EventPriority.LOWEST)
    fun onSourceClosed(event: AlSourceClosedEvent) {
        coroutineScope.launch {
            mutex.withLock {
                sources.remove(event.source)
            }
        }
    }

    private fun openSync() {
        if (isOpen()) throw DeviceException("Device is already open")

        DevicePreOpenEvent(this@AlOutputDevice).also {
            if (!voiceClient.eventBus.fire(it)) throw DeviceException("Device opening has been canceled")
        }

        devicePointer = openDevice(name)
        hasDisconnectEXT = ALC10.alcIsExtensionPresent(devicePointer, "ALC_EXT_disconnect")

        val aLCCapabilities = ALC.createCapabilities(devicePointer)
        if (AlUtil.checkAlcErrors(devicePointer, "Get capabilities")) {
            throw DeviceException("Failed to get OpenAL capabilities")
        } else if (!aLCCapabilities.OpenALC11) {
            throw DeviceException("OpenAL 1.1 not supported")
        }

        contextPointer = ALC11.alcCreateContext(devicePointer, null as IntBuffer?)
        EXTThreadLocalContext.alcSetThreadContext(contextPointer)

        val aLCapabilities = AL.createCapabilities(aLCCapabilities)
        AlUtil.checkErrors("Initialization")
        if (!aLCapabilities.AL_EXT_source_distance_model) {
            throw DeviceException("AL_EXT_source_distance_model is not supported")
        }

        hrtfSupported = aLCCapabilities.ALC_SOFT_HRTF
        if (hrtfSupported && voiceClient.config.voice.hrtf.value()) {
            enableHrtf()
        }

        AL10.alEnable(512)
        if (!aLCapabilities.AL_EXT_LINEAR_DISTANCE) {
            throw DeviceException("AL_EXT_LINEAR_DISTANCE is not supported")
        }
        AlUtil.checkErrors("Enable per-source distance models")

        LOGGER.info("Device $name initialized")

        AL11.alListenerf(AL11.AL_GAIN, 1.0f)
        AL11.alListener3f(AL11.AL_POSITION, 0.0f, 0.0f, 0.0f)
        AL11.alListenerfv(
            AL11.AL_ORIENTATION, floatArrayOf(
                0.0f, 0.0f, -1.0f,
                0.0f, 1.0f, 0.0f
            )
        )
        listener.start()

        voiceClient.eventBus.fire(DeviceOpenEvent(this@AlOutputDevice))
    }

    private suspend fun closeSync() {
        closeSources()
        listener.stop()

        EXTThreadLocalContext.alcSetThreadContext(0L)

        if (contextPointer != 0L) {
            ALC11.alcDestroyContext(contextPointer)
        }
        if (devicePointer != 0L && !disconnected) {
            ALC11.alcCloseDevice(devicePointer)
        }

        contextPointer = 0L
        devicePointer = 0L

        LOGGER.info("Device $name closed")
        voiceClient.eventBus.fire(DeviceClosedEvent(this@AlOutputDevice))
    }

    @Throws(DeviceException::class)
    private fun openDevice(deviceName: String?): Long {
        val devicePointer = ALC11.alcOpenDevice(deviceName)
        if (devicePointer == 0L || AlUtil.checkAlcErrors(devicePointer, "Open device")) {
            throw IllegalStateException("Failed to open OpenAL device")
        }

        return devicePointer
    }

    override fun isHrtfSupported(): Boolean {
        val num = ALC11.alcGetInteger(devicePointer, SOFTHRTF.ALC_NUM_HRTF_SPECIFIERS_SOFT)
        return num > 0
    }

    override fun isHrtfEnabled(): Boolean {
        val state = ALC11.alcGetInteger(devicePointer, SOFTHRTF.ALC_HRTF_SOFT)
        return state > 0
    }

    override fun enableHrtf() {
        if (!isHrtfSupported) return

        toggleHrtf(true)

        if (isHrtfEnabled) {
            val name = ALC11.alcGetString(devicePointer, SOFTHRTF.ALC_HRTF_SPECIFIER_SOFT)
            LOGGER.info("HRTF enabled, using {}", name)
        } else {
            LOGGER.warn("Failed to enable HRTF")
        }
    }

    override fun disableHrtf() {
        if (!isHrtfEnabled || !isHrtfEnabled) return

        toggleHrtf(false)
        LOGGER.info("HRTF disabled")
    }

    private fun toggleHrtf(enabled: Boolean) {
        val attr = BufferUtils.createIntBuffer(10)
            .put(SOFTHRTF.ALC_HRTF_SOFT)
            .put(if (enabled) ALC10.ALC_TRUE else ALC10.ALC_FALSE)
            .put(SOFTHRTF.ALC_HRTF_ID_SOFT)
            .put(0)
            .put(0)
        (attr as Buffer).flip()

        if (!SOFTHRTF.alcResetDeviceSOFT(devicePointer, attr)) {
            LOGGER.warn(
                "Failed to reset device: {}",
                ALC11.alcGetString(devicePointer, ALC11.alcGetError(devicePointer))
            )
        }
    }

    private inner class AlListener {

        private val rotation = Quaternionf(0.0f, 0.0f, 0.0f, 1.0f)
        private val forwards = Vector3f(0.0f, 0.0f, 1.0f)
        private val up = Vector3f(0.0f, 1.0f, 0.0f)

        private var job: Job? = null

        fun start() {
            job = coroutineScope.launch {
                while (true) {
                    update()
                    delay(5L)
                }
            }
        }

        fun stop() {
            job?.cancel()
        }

        fun update() {
            val position: Vec3
            val lookVector: Vector3f
            val upVector: Vector3f

            if (voiceClient.config.advanced.cameraSoundListener.value()
                && voiceClient.serverInfo.orElse(null)
                    ?.playerInfo
                    ?.get("pv.allow_freecam")
                    ?.orElse(true) == true
            ) {
                val camera = Minecraft.getInstance().gameRenderer.mainCamera

                position = camera.position
                lookVector = camera.lookVector
                upVector = camera.upVector
            } else {
                val player = Minecraft.getInstance().player ?: return
                position = player.eyePosition()

                rotation[0.0f, 0.0f, 0.0f] = 1.0f

                val YP = Vector3f(0.0f, 1.0f, 0.0f)
                val XP = Vector3f(1.0f, 0.0f, 0.0f)

                //#if MC>=11903
                rotation.rotateAxis(-player.yRot, YP)
                rotation.rotateAxis(player.xRot, XP)
                //#else
                //$$ rotation.mul(YP.rotationDegrees(-player.yRot));
                //$$ rotation.mul(XP.rotationDegrees(player.xRot));
                //#endif

                forwards[0.0f, 0.0f] = 1.0f
                forwards.rotate(rotation)
                up[0.0f, 1.0f] = 0.0f
                up.rotate(rotation)

                lookVector = forwards
                upVector = up
            }

            AL11.alListener3f(
                AL11.AL_POSITION, position.x().toFloat(), position.y().toFloat(), position.z().toFloat()
            )

            AL11.alListenerfv(
                AL11.AL_ORIENTATION, floatArrayOf(
                    lookVector.x(), lookVector.y(), lookVector.z(),
                    upVector.x(), upVector.y(), upVector.z()
                )
            )
        }
    }

    companion object {
        private val LOGGER = LogManager.getLogger(AlOutputDevice::class.java)
    }
}
