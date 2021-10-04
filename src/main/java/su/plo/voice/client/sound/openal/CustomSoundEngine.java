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
import su.plo.voice.client.socket.SocketClientUDPQueue;
import su.plo.voice.client.sound.AbstractSoundQueue;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
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
    protected boolean soundPhysics;
    private ScheduledExecutorService executor;

    public CustomSoundEngine() {
        this.listener = new Listener();
    }

    public void toggleHRTF() {
        // kill all queues to prevent possible problems
        SocketClientUDPQueue.audioChannels
                .values()
                .forEach(AbstractSoundQueue::closeAndKill);
        SocketClientUDPQueue.audioChannels.clear();

        VoiceClient.getSoundEngine().close();
        new Thread(() -> VoiceClient.getSoundEngine().init(true)).start();
    }

    public CustomSource createSource() {
        return CustomSource.create();
    }

    public void init(boolean inThread) {
        this.preInit();

        this.devicePointer = openDevice();
        ALCCapabilities aLCCapabilities = ALC.createCapabilities(this.devicePointer);
        if (AlUtil.checkAlcErrors(this.devicePointer, "Get capabilities")) {
            throw new IllegalStateException("Failed to get OpenAL capabilities");
        } else if (!aLCCapabilities.OpenALC11) {
            throw new IllegalStateException("OpenAL 1.1 not supported");
        } else {
            this.contextPointer = ALC10.alcCreateContext(this.devicePointer, (IntBuffer) null);

            if(!inThread) {
                ALC10.alcMakeContextCurrent(this.contextPointer);
            } else {
                EXTThreadLocalContext.alcSetThreadContext(this.contextPointer);
            }

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

        executor = Executors.newScheduledThreadPool(1);
        executor.schedule(() -> {
            EXTThreadLocalContext.alcSetThreadContext(this.contextPointer);
        }, 0L, TimeUnit.MILLISECONDS);
        executor.scheduleAtFixedRate(() -> {
            Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
            Vec3 vec3d = camera.getPosition();
            Vector3f vector3f = camera.getLookVector();
            Vector3f vector3f2 = camera.getUpVector();

            listener.setListenerPosition(vec3d);
            listener.setListenerOrientation(vector3f, vector3f2);
        }, 0L, 5L, TimeUnit.MILLISECONDS);

        if(inThread) {
            EXTThreadLocalContext.alcSetThreadContext(0L);
        }

        synchronized (this) {
            this.notifyAll();
        }
    }

    private static long openDevice() {
        for(int i = 0; i < 3; ++i) {
            long l = ALC10.alcOpenDevice((ByteBuffer)null);
            if (l != 0L && !AlUtil.checkAlcErrors(l, "Open device")) {
                return l;
            }
        }

        throw new IllegalStateException("Failed to open OpenAL device");
    }

    public void close() {
        if (this.initialized) {
            this.executor.schedule(() -> {
                EXTThreadLocalContext.alcSetThreadContext(0L);
                this.executor.shutdown();
            }, 0L, TimeUnit.MILLISECONDS);
            this.initialized = false;
            ALC10.alcDestroyContext(this.contextPointer);
            if (this.devicePointer != 0L) {
                ALC10.alcCloseDevice(this.devicePointer);
            }

            this.contextPointer = 0L;
            this.devicePointer = 0L;
        }
    }

    public void preInit() {
    }

    public void postInit() {
    }
}
