package su.plo.voice.client.audio.device;

import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import lombok.Getter;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.*;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.audio.device.*;
import su.plo.voice.api.client.audio.device.source.AlSource;
import su.plo.voice.api.client.event.audio.device.DeviceClosedEvent;
import su.plo.voice.api.client.event.audio.device.DeviceOpenEvent;
import su.plo.voice.api.client.event.audio.device.DevicePreOpenEvent;
import su.plo.voice.api.client.event.audio.device.source.AlSourceClosedEvent;
import su.plo.voice.api.event.EventPriority;
import su.plo.voice.api.event.EventSubscribe;
import su.plo.voice.api.util.Params;
import su.plo.voice.client.audio.AlUtil;
import su.plo.voice.client.audio.device.source.StreamAlSource;

import javax.sound.sampled.AudioFormat;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.*;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.lwjgl.openal.ALC10.ALC_FALSE;
import static org.lwjgl.openal.ALC11.ALC_TRUE;

public final class AlOutputDevice extends BaseAudioDevice implements AlAudioDevice, AlListenerDevice, HrtfAudioDevice, OutputDevice<AlSource> {

    private static final Logger LOGGER = LogManager.getLogger(AlOutputDevice.class);

    private final PlasmoVoiceClient client;
    private final @Nullable String name;

    private final ExecutorService executor;
    @Getter
    private final Listener listener = new AlListener();
    private final Set<AlSource> sources = new CopyOnWriteArraySet<>();

    private AudioFormat format;
    private Params params;
    private boolean hrtfSupported;
    @Getter
    private int bufferSize;
    private long devicePointer;
    private long contextPointer;

    public AlOutputDevice(PlasmoVoiceClient client, @Nullable String name) {
        this.client = client;
        this.name = name;
        this.executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(
                    null,
                    r,
                    "Al Output Device " + name,
                    0
            );
            if (thread.isDaemon()) thread.setDaemon(false);
            if (thread.getPriority() != Thread.NORM_PRIORITY) thread.setPriority(Thread.NORM_PRIORITY);

            return thread;
        });
    }

    @Override
    public void open(@NotNull AudioFormat format, @NotNull Params params) throws DeviceException {
        checkNotNull(params, "params cannot be null");

        DevicePreOpenEvent preOpenEvent = new DevicePreOpenEvent(this, params);
        client.getEventBus().call(preOpenEvent);

        if (preOpenEvent.isCancelled()) {
            throw new DeviceException("Device opening has been canceled");
        }

        if (isOpen()) {
            throw new DeviceException("Device already open");
        }

        runInContext(() -> {
            synchronized (this) {
                this.devicePointer = openDevice(name);
                this.format = format;
                this.params = params;
                this.bufferSize = ((int) format.getSampleRate() / 1_000) * 20;

                ALCCapabilities aLCCapabilities = ALC.createCapabilities(devicePointer);
                if (AlUtil.checkAlcErrors(devicePointer, "Get capabilities")) {
                    throw new DeviceException("Failed to get OpenAL capabilities");
                } else if (!aLCCapabilities.OpenALC11) {
                    throw new DeviceException("OpenAL 1.1 not supported");
                }

                this.contextPointer = ALC11.alcCreateContext(this.devicePointer, (IntBuffer) null);
                EXTThreadLocalContext.alcSetThreadContext(this.contextPointer);

                ALCapabilities aLCapabilities = AL.createCapabilities(aLCCapabilities);
                AlUtil.checkErrors("Initialization");
                if (!aLCapabilities.AL_EXT_source_distance_model) {
                    throw new DeviceException("AL_EXT_source_distance_model is not supported");
                }

                this.hrtfSupported = aLCCapabilities.ALC_SOFT_HRTF;

                if (params.containsKey("hrtf") && hrtfSupported) {
                    Object hrtf = params.get("hrtf");
                    if (hrtf.equals(true)) enableHrtf();
                }

                AL10.alEnable(512);
                if (!aLCapabilities.AL_EXT_LINEAR_DISTANCE) {
                    throw new DeviceException("AL_EXT_LINEAR_DISTANCE is not supported");
                }

                AlUtil.checkErrors("Enable per-source distance models");
                LOGGER.info("Device " + name + " initialized");

                AL11.alListenerf(AL11.AL_GAIN, 1.0F);

                AL11.alListener3f(AL11.AL_POSITION, 0.0F, 0.0F, 0.0F);
                AL11.alListenerfv(AL11.AL_ORIENTATION, new float[]{
                        0.0F, 0.0F, -1.0F,
                        0.0F, 1.0F, 0.0F
                });

                client.getEventBus().call(new DeviceOpenEvent(this));
            }
        });
    }

    @Override
    public void close() {
        if (!isOpen()) return;

        closeSources();
        runInContext(() -> {
            synchronized (this) {
                EXTThreadLocalContext.alcSetThreadContext(0L);

                ALC11.alcDestroyContext(contextPointer);
                if (devicePointer != 0L) {
                    ALC11.alcCloseDevice(devicePointer);
                }

                this.contextPointer = 0L;
                this.devicePointer = 0L;

                LOGGER.info("Device " + name + " closed");

                client.getEventBus().call(new DeviceClosedEvent(this));
            }
        });
    }

    @Override
    public boolean isOpen() {
        return devicePointer != 0L;
    }

    @Override
    public @Nullable String getName() {
        return name;
    }

    @Override
    public Optional<AudioFormat> getFormat() {
        return Optional.ofNullable(format);
    }

    @Override
    public Optional<Params> getParams() {
        return Optional.ofNullable(params);
    }

    @Override
    public AlSource createSource(boolean stereo, @NotNull Params params) throws DeviceException {
        checkNotNull(params, "params cannot be null");
        if (!isOpen()) throw new DeviceException("Device is not open");

        int numBuffers = 0;
        if (params.containsKey("numBuffers")) {
            try {
                numBuffers = params.get("numBuffers");
            } catch (IllegalArgumentException e) {
                throw new DeviceException(e);
            }

            if (numBuffers < 4) {
                throw new DeviceException("Min number of buffers is 4");
            } else if (numBuffers > 64) {
                throw new DeviceException("Max number of buffers is 64");
            }
        }

        try {
            AlSource source = StreamAlSource.create(this, client, stereo, numBuffers);
            sources.add(source);
            return source;
        } catch (RuntimeException e) {
            if (e.getCause() instanceof DeviceException) {
                throw (DeviceException) e.getCause();
            }

            throw new DeviceException("Failed to allocate new source", e);
        }
    }

    @Override
    public void closeSources() {
        sources.forEach(AlSource::close);
    }

    @Override
    public void reload(@Nullable AudioFormat format, @NotNull Params params) throws DeviceException {
        if (devicePointer == 0L) {
            throw new DeviceException("Device is not open");
        }

        if (format == null) {
            if (this.format == null) throw new DeviceException("Device is not open");
            format = this.format;
        }

        checkNotNull(params);

        Params.Builder paramsBuilder = Params.builder();
        this.params.entrySet().forEach(
                (entry) -> paramsBuilder.set(entry.getKey(), entry.getValue())
        );
        params.entrySet().forEach(
                (entry) -> paramsBuilder.set(entry.getKey(), entry.getValue())
        );

        close();
        open(format, paramsBuilder.build());
    }

    @Override
    public Optional<Long> getPointer() {
        return devicePointer <= 0 ? Optional.empty() : Optional.of(devicePointer);
    }

    @Override
    public Optional<Long> getContextPointer() {
        return contextPointer <= 0 ? Optional.empty() : Optional.of(contextPointer);
    }

    @Override
    public void runInContext(@NotNull DeviceRunnable runnable) {
        try {
            if (AlUtil.sameDeviceContext(this)) {
                runnable.run();
                return;
            }

            CompletableFuture<Void> future = new CompletableFuture<>();

            executor.execute(() -> {
                try {
                    runnable.run();
                } catch (Exception e) {
                    future.completeExceptionally(e);
                } finally {
                    future.complete(null);
                }
            });

            future.get();
        } catch (DeviceException | ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @EventSubscribe(priority = EventPriority.LOWEST)
    public void onSourceClosed(@NotNull AlSourceClosedEvent event) {
        sources.remove(event.getSource());
    }

    private long openDevice(String deviceName) throws DeviceException {
        long l;
        if (deviceName == null) {
            // default device
            l = ALC11.alcOpenDevice((ByteBuffer) null);
        } else {
            l = ALC11.alcOpenDevice(deviceName);
        }

        if (l != 0L && !AlUtil.checkAlcErrors(l, "Open device")) {
            return l;
        }

        throw new IllegalStateException("Failed to open OpenAL device");
    }

    @Override
    public boolean isHrtfSupported() {
        int num = ALC11.alcGetInteger(devicePointer, SOFTHRTF.ALC_NUM_HRTF_SPECIFIERS_SOFT);
        return num > 0;
    }

    @Override
    public boolean isHrtfEnabled() {
        int state = ALC11.alcGetInteger(devicePointer, SOFTHRTF.ALC_HRTF_SOFT);
        return state > 0;
    }

    @Override
    public void enableHrtf() {
        if (!isHrtfSupported()) return;

        toggleHrtf(true);

        if (isHrtfEnabled()) {
            String name = ALC11.alcGetString(devicePointer, SOFTHRTF.ALC_HRTF_SPECIFIER_SOFT);
            LOGGER.info("HRTF enabled, using {}", name);
        } else {
            LOGGER.warn("Failed to enable HRTF");
        }
    }

    @Override
    public void disableHrtf() {
        if (!isHrtfEnabled() || !isHrtfEnabled()) return;

        toggleHrtf(false);
        LOGGER.info("HRTF disabled");
    }

    private void toggleHrtf(boolean enabled) {
        IntBuffer attr = BufferUtils.createIntBuffer(10)
                .put(SOFTHRTF.ALC_HRTF_SOFT)
                .put(enabled ? ALC_TRUE : ALC_FALSE)
                .put(SOFTHRTF.ALC_HRTF_ID_SOFT)
                .put(0)
                .put(0);
        ((Buffer) attr).flip();

        if (!SOFTHRTF.alcResetDeviceSOFT(devicePointer, attr)) {
            LOGGER.warn("Failed to reset device: {}", ALC11.alcGetString(devicePointer, ALC11.alcGetError(devicePointer)));
        }
    }

    private class AlListener implements Listener {

        private final Quaternion rotation = new Quaternion(0.0F, 0.0F, 0.0F, 1.0F);

        private final Vector3f forwards = new Vector3f(0.0F, 0.0F, 1.0F);
        private final Vector3f up = new Vector3f(0.0F, 1.0F, 0.0F);

        @Override
        public void update() {
            executor.execute(() -> {
                Vec3 position;
                Vector3f lookVector, upVector;

                if (params.get("listenerCameraRelative")) {
                    Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();

                    position = camera.getPosition();
                    lookVector = camera.getLookVector();
                    upVector = camera.getUpVector();
                } else {
                    LocalPlayer player = Minecraft.getInstance().player;
                    if (player == null) return;

                    position = player.getEyePosition();


                    rotation.set(0.0F, 0.0F, 0.0F, 1.0F);
                    rotation.mul(Vector3f.YP.rotationDegrees(-player.getYRot()));
                    rotation.mul(Vector3f.XP.rotationDegrees(player.getXRot()));

                    forwards.set(0.0F, 0.0F, 1.0F);
                    forwards.transform(rotation);
                    up.set(0.0F, 1.0F, 0.0F);
                    up.transform(rotation);

                    lookVector = forwards;
                    upVector = up;
                }

                AL11.alListener3f(
                        AL11.AL_POSITION,
                        (float) position.x(),
                        (float) position.y(),
                        (float) position.z()
                );
                AL11.alListenerfv(AL11.AL_ORIENTATION, new float[]{
                        lookVector.x(), lookVector.y(), lookVector.z(),
                        upVector.x(), upVector.y(), upVector.z()
                });
            });
        }
    }
}
