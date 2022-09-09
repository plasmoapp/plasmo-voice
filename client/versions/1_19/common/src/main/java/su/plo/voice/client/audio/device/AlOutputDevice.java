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
import su.plo.voice.api.client.audio.device.AlAudioDevice;
import su.plo.voice.api.client.audio.device.AudioDevice;
import su.plo.voice.api.client.audio.device.DeviceException;
import su.plo.voice.api.client.audio.device.OutputDevice;
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
import static org.lwjgl.openal.ALC10.ALC_TRUE;

public final class AlOutputDevice extends BaseAudioDevice implements AlAudioDevice, OutputDevice<AlSource> {

    private static final Logger LOGGER = LogManager.getLogger(AlOutputDevice.class);

    private final PlasmoVoiceClient client;
    private final @Nullable String name;

    private final ScheduledExecutorService executor;
    private final Set<AlSource> sources = new CopyOnWriteArraySet<>();

    private AudioFormat format;
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
    public CompletableFuture<AudioDevice> open(@NotNull AudioFormat format, @NotNull Params params) throws DeviceException {
        checkNotNull(params, "params cannot be null");

        DevicePreOpenEvent preOpenEvent = new DevicePreOpenEvent(this, params);
        client.getEventBus().call(preOpenEvent);

        if (preOpenEvent.isCancelled()) {
            throw new DeviceException("Device opening has been canceled");
        }

        if (isOpen()) {
            throw new DeviceException("Device already open");
        }

        CompletableFuture<AudioDevice> future = new CompletableFuture<>();
        runInContext(() -> {
            try {
                this.devicePointer = openDevice(name);
                this.format = format;
                this.bufferSize = ((int) format.getSampleRate() / 1_000) * 20;

                ALCCapabilities aLCCapabilities = ALC.createCapabilities(devicePointer);
                if (AlUtil.checkAlcErrors(devicePointer, "Get capabilities")) {
                    throw new DeviceException("Failed to get OpenAL capabilities");
                } else if (!aLCCapabilities.OpenALC11) {
                    throw new DeviceException("OpenAL 1.1 not supported");
                }

                this.contextPointer = ALC10.alcCreateContext(this.devicePointer, (IntBuffer) null);
                EXTThreadLocalContext.alcSetThreadContext(this.contextPointer);

                ALCapabilities aLCapabilities = AL.createCapabilities(aLCCapabilities);
                AlUtil.checkErrors("Initialization");
                if (!aLCapabilities.AL_EXT_source_distance_model) {
                    throw new DeviceException("AL_EXT_source_distance_model is not supported");
                }

                AL10.alEnable(512);
                if (!aLCapabilities.AL_EXT_LINEAR_DISTANCE) {
                    throw new DeviceException("AL_EXT_LINEAR_DISTANCE is not supported");
                }

                AlUtil.checkErrors("Enable per-source distance models");
                LOGGER.info("Device " + name + " initialized");

                if (params.containsKey("hrtf")) {
                    Object hrtf = params.get("hrtf");
                    if (hrtf.equals(true)) {
                        enableHRTF();
                    }
                }

                AL11.alListenerf(AL11.AL_GAIN, 1.0F);

                AL11.alListener3f(AL11.AL_POSITION, 0.0F, 0.0F, 0.0F);
                AL11.alListenerfv(AL11.AL_ORIENTATION, new float[]{
                        0.0F, 0.0F, -1.0F,
                        0.0F, 1.0F, 0.0F
                });

                final Quaternion rotation = new Quaternion(0.0F, 0.0F, 0.0F, 1.0F);

                final Vector3f forwards = new Vector3f(0.0F, 0.0F, 1.0F);
                final Vector3f up = new Vector3f(0.0F, 1.0F, 0.0F);

                executor.scheduleAtFixedRate(() -> {
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
                }, 0L, 5L, TimeUnit.MILLISECONDS);

                client.getEventBus().call(new DeviceOpenEvent(this));

                future.complete(this);
            } catch (DeviceException e) {
                future.completeExceptionally(e);
            }
        });

        return future;
    }

    @Override
    public CompletableFuture<AudioDevice> close() {
        CompletableFuture<AudioDevice> future = new CompletableFuture<>();

        if (isOpen()) {
            for (AlSource source : sources) {
                try {
                    source.close().get();
                } catch (InterruptedException | ExecutionException e) {
                    LOGGER.error("Failed to close alSource", e);
                    future.completeExceptionally(e);
                    return future;
                }
            }

            runInContext(() -> {
                EXTThreadLocalContext.alcSetThreadContext(0L);

                ALC10.alcDestroyContext(contextPointer);
                if (devicePointer != 0L) {
                    ALC10.alcCloseDevice(devicePointer);
                }

                this.contextPointer = 0L;
                this.devicePointer = 0L;

                LOGGER.info("Device " + name + " closed");

                client.getEventBus().call(new DeviceClosedEvent(this));
            });
        } else {
            future.complete(this);
        }

        return future;
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

        CompletableFuture<AlSource> source = StreamAlSource.create(this, client, stereo, numBuffers);
        try {
            AlSource alSource = source.get();
            sources.add(alSource);
            return alSource;
        } catch (InterruptedException e) {
            throw new DeviceException("Failed to allocate new source", e);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof DeviceException) {
                throw (DeviceException) e.getCause();
            }

            throw new DeviceException("Failed to allocate new source", e);
        }
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
    public void runInContext(Runnable runnable) {
        executor.execute(runnable);
    }

    @EventSubscribe(priority = EventPriority.LOWEST)
    public void onSourceClosed(AlSourceClosedEvent event) {
        sources.remove(event.getSource());
    }

    private long openDevice(String deviceName) throws DeviceException {
        long l;
        if (deviceName == null) {
            // default device
            l = ALC10.alcOpenDevice((ByteBuffer) null);
        } else {
            l = ALC10.alcOpenDevice(deviceName);
        }

        if (l != 0L && !AlUtil.checkAlcErrors(l, "Open device")) {
            return l;
        }

        throw new IllegalStateException("Failed to open OpenAL device");
    }

    private void enableHRTF() throws DeviceException {
        int num = ALC10.alcGetInteger(devicePointer, SOFTHRTF.ALC_NUM_HRTF_SPECIFIERS_SOFT);
        if (num <= 0) throw new DeviceException("HRTF is not supported");

        IntBuffer attr = BufferUtils.createIntBuffer(10)
                .put(SOFTHRTF.ALC_HRTF_SOFT)
                .put(ALC_TRUE);

        attr.put(0);
        ((Buffer) attr).flip();

        if (!SOFTHRTF.alcResetDeviceSOFT(devicePointer, attr)) {
            LOGGER.warn("Failed to reset device: {}", ALC10.alcGetString(devicePointer, ALC10.alcGetError(devicePointer)));
        }

        int state = ALC10.alcGetInteger(devicePointer, SOFTHRTF.ALC_HRTF_SOFT);
        if (state != 0) {
            String name = ALC10.alcGetString(devicePointer, SOFTHRTF.ALC_HRTF_SPECIFIER_SOFT);
            LOGGER.info("HRTF enabled, using {}", name);
        }
    }
}
