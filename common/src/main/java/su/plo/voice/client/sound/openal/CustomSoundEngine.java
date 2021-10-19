package su.plo.voice.client.sound.openal;

import com.mojang.blaze3d.audio.Listener;
import com.mojang.math.Vector3f;
import lombok.Getter;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.*;
import su.plo.voice.client.VoiceClient;
import su.plo.voice.client.gui.VoiceSettingsScreen;
import su.plo.voice.client.socket.SocketClientUDPQueue;
import su.plo.voice.client.sound.AbstractSoundQueue;
import su.plo.voice.client.sound.Recorder;
import su.plo.voice.client.sound.capture.JavaxCaptureDevice;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.lwjgl.openal.ALC10.ALC_TRUE;

public class CustomSoundEngine {
    private static final Logger LOGGER = LogManager.getLogger();
    private long devicePointer;
    @Getter
    private long contextPointer;
    @Getter
    public final Listener listener;
    public boolean initialized;
    @Getter
    private boolean hrtfSupported;
    @Getter
    private final ScheduledExecutorService executor;

    public CustomSoundEngine() {
        this.listener = new Listener();
        this.executor = Executors.newScheduledThreadPool(1);
    }

    public void restart() {
        SocketClientUDPQueue.audioChannels
                .values()
                .forEach(AbstractSoundQueue::closeAndKill);
        SocketClientUDPQueue.audioChannels.clear();

        this.close();
        this.init();
    }

    public CustomSource createSource() {
        return this.initialized ? CustomSource.create() : null;
    }

    public void runInContext(Runnable runnable) {
        executor.submit(runnable);
    }

    public void init() {
        this.runInContext(this::initSync);
    }

    public void initSync() {
        this.preInit();

        this.devicePointer = openDevice();
        ALCCapabilities aLCCapabilities = ALC.createCapabilities(this.devicePointer);
        if (AlUtil.checkAlcErrors(this.devicePointer, "Get capabilities")) {
            throw new IllegalStateException("Failed to get OpenAL capabilities");
        } else if (!aLCCapabilities.OpenALC11) {
            throw new IllegalStateException("OpenAL 1.1 not supported");
        } else {
            this.contextPointer = ALC10.alcCreateContext(this.devicePointer, (IntBuffer) null);

            EXTThreadLocalContext.alcSetThreadContext(this.contextPointer);

            ALCapabilities aLCapabilities = AL.createCapabilities(aLCCapabilities);
            AlUtil.checkErrors("Initialization");
            if (!aLCapabilities.AL_EXT_source_distance_model) {
                throw new IllegalStateException("AL_EXT_source_distance_model is not supported");
            } else {
                AL10.alEnable(512);
                if (!aLCapabilities.AL_EXT_LINEAR_DISTANCE) {
                    throw new IllegalStateException("AL_EXT_LINEAR_DISTANCE is not supported");
                } else {
                    AlUtil.checkErrors("Enable per-source distance models");
                    LOGGER.info("OpenAL (Plasmo Voice) initialized.");
                }
            }

            int num_hrtf = ALC10.alcGetInteger(this.devicePointer, SOFTHRTF.ALC_NUM_HRTF_SPECIFIERS_SOFT);
            if (num_hrtf > 0) {
                this.hrtfSupported = true;
            }

            if(VoiceClient.getClientConfig().hrtf.get()) {
                if(num_hrtf > 0) {
                    IntBuffer attr = BufferUtils.createIntBuffer(10)
                            .put(SOFTHRTF.ALC_HRTF_SOFT)
                            .put(ALC_TRUE);

                    attr.put(0);
                    ((Buffer) attr).flip();

                    if (!SOFTHRTF.alcResetDeviceSOFT(this.devicePointer, attr)) {
                        LOGGER.info("Failed to reset device: {}", ALC10.alcGetString(this.devicePointer, ALC10.alcGetError(this.devicePointer)));
                    }

                    int hrtf_state = ALC10.alcGetInteger(this.devicePointer, SOFTHRTF.ALC_HRTF_SOFT);
                    if (hrtf_state != 0) {
                        String name = ALC10.alcGetString(this.devicePointer, SOFTHRTF.ALC_HRTF_SPECIFIER_SOFT);
                        LOGGER.info("HRTF enabled, using {}", name);
                    }
                }
            }
        }

        this.listener.reset();
        this.listener.setGain(1.0F);

        this.initialized = true;
        this.postInit();

        executor.scheduleAtFixedRate(() -> {
            Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
            Vec3 vec3d = camera.getPosition();
            Vector3f vector3f = camera.getLookVector();
            Vector3f vector3f2 = camera.getUpVector();

            listener.setListenerPosition(vec3d);
            listener.setListenerOrientation(vector3f, vector3f2);
        }, 0L, 5L, TimeUnit.MILLISECONDS);

        synchronized (this) {
            this.notifyAll();
        }
    }

    public synchronized void close() {
        if (this.initialized) {
            this.executor.submit(() -> {
                if (Minecraft.getInstance().screen instanceof VoiceSettingsScreen) {
                    ((VoiceSettingsScreen) Minecraft.getInstance().screen).closeSpeaker();
                }

                EXTThreadLocalContext.alcSetThreadContext(0L);
            });

            this.initialized = false;
            ALC10.alcDestroyContext(this.contextPointer);
            if (this.devicePointer != 0L) {
                ALC10.alcCloseDevice(this.devicePointer);
            }

            this.contextPointer = 0L;
            this.devicePointer = 0L;
        }
    }

    // devices
    private long openDevice() {
        try {
            return openDevice(getCurrentDevice());
        } catch (IllegalStateException ignored) {
            try {
                return openDevice(null);
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }

        throw new IllegalStateException("Failed to open OpenAL device");
    }

    private long openDevice(String deviceName) {
        long l;
        if (deviceName == null) {
            // default device
            l = ALC10.alcOpenDevice((ByteBuffer)null);
        } else {
            l = ALC10.alcOpenDevice(deviceName);
        }

        if (l != 0L && !AlUtil.checkAlcErrors(l, "Open device")) {
            return l;
        }

        throw new IllegalStateException("Failed to open OpenAL device");
    }

    public static String getCurrentDevice() {
        String deviceName = VoiceClient.getClientConfig().speaker.get();
        List<String> devices = getDevices();
        if (deviceName == null || !devices.contains(deviceName)) {
            deviceName = getDefaultDevice();
        }

        return deviceName;
    }

    public static String getDefaultDevice() {
        return ALC11.alcGetString(0L, ALC11.ALC_ALL_DEVICES_SPECIFIER);
    }

    public static List<String> getDevices() {
        return ALUtil.getStringList(0L, ALC11.ALC_ALL_DEVICES_SPECIFIER);
    }

    // capture devices
    public static long openCaptureDevice() {
        try {
            return openCaptureDevice(getCurrentCaptureDevice());
        } catch (IllegalStateException ignored) {
            try {
                return openCaptureDevice(null);
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }

        throw new IllegalStateException("Failed to open OpenAL capture device");
    }

    private static long openCaptureDevice(String deviceName) {
        long l;
        if (deviceName == null) {
            // default device
            l = ALC11.alcCaptureOpenDevice((ByteBuffer)null, Recorder.getSampleRate(), AL11.AL_FORMAT_MONO16, Recorder.getFrameSize());
        } else {
            l = ALC11.alcCaptureOpenDevice(deviceName, Recorder.getSampleRate(), AL11.AL_FORMAT_MONO16, Recorder.getFrameSize());
        }

        if (l != 0L && !AlUtil.checkAlcErrors(l, "Open device")) {
            return l;
        }

        throw new IllegalStateException("Failed to open OpenAL device");
    }

    public static String getCurrentCaptureDevice() {
        String deviceName = VoiceClient.getClientConfig().microphone.get();
        if (VoiceClient.getClientConfig().javaxCapture.get()) {
            List<String> devices = JavaxCaptureDevice.getNames();
            if (deviceName == null || !devices.contains(deviceName)) {
                deviceName = devices.get(0);
            }
        } else {
            List<String> devices = getCaptureDevices();
            if (deviceName == null || !devices.contains(deviceName)) {
                deviceName = getDefaultCaptureDevice();
            }
        }

        return deviceName;
    }

    public static String getDefaultCaptureDevice() {
        if (VoiceClient.getClientConfig().javaxCapture.get()) {
            return JavaxCaptureDevice.getNames().get(0);
        } else {
            String deviceName = ALC11.alcGetString(0L, ALC11.ALC_CAPTURE_DEVICE_SPECIFIER);
            AlUtil.checkErrors("Get default capture");
            return deviceName;
        }
    }

    public static List<String> getCaptureDevices() {
        if (VoiceClient.getClientConfig().javaxCapture.get()) {
            return JavaxCaptureDevice.getNames();
        } else {
            List<String> devices = ALUtil.getStringList(0L, ALC11.ALC_CAPTURE_DEVICE_SPECIFIER);
            AlUtil.checkErrors("Get capture devices");
            return devices == null ? Collections.emptyList() : devices;
        }
    }

    public void preInit() {
    }

    public void postInit() {
    }
}
