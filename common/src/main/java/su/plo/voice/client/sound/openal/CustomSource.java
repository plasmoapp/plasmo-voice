package su.plo.voice.client.sound.openal;

import lombok.Setter;
import lombok.SneakyThrows;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.openal.AL10;
import org.lwjgl.system.MemoryUtil;
import su.plo.voice.client.sound.Recorder;

import javax.sound.sampled.AudioFormat;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

public class CustomSource {
    protected final int pointer;
    private final AtomicBoolean playing = new AtomicBoolean(true);

    private final int format;
    private final LinkedList<Integer> freeBuffers = new LinkedList<>();
    protected Vec3 pos;

    @Setter
    private boolean reverbOnly;
    private long lastEnvCalculated;
    private Vec3 lastEnvPos;

    @Nullable
    static CustomSource create() {
        int[] is = new int[1];
        AL10.alGenSources(is);

        return AlUtil.checkErrors("Allocate new source") ? null : new CustomSource(is[0]);
    }

    protected CustomSource(int pointer) {
        this.pointer = pointer;
        int[] buffers = new int[16];
        AL10.alGenBuffers(buffers);
        AlUtil.checkErrors("Creating buffers");

        for (int buffer : buffers) {
            freeBuffers.offer(buffer);
        }

        this.format = AlUtil.getFormatId(Recorder.getFormat());
    }

    public int getPointer() {
        return pointer;
    }

    public void close() {
        if (this.playing.compareAndSet(true, false)) {
            AL10.alSourceStop(this.pointer);
            AlUtil.checkErrors("Stop");
            this.removeProcessedBuffers();

            while (!freeBuffers.isEmpty()) {
                AL10.alDeleteBuffers(freeBuffers.poll());
            }

            AL10.alDeleteSources(new int[]{this.pointer});
            AlUtil.checkErrors("Cleanup");
        }
    }

    @SneakyThrows
    public void play() {
        if (pos == null) {
            return;
        }

        if (CustomSoundEngine.soundPhysicsPlaySoundNew != null) {
            if (System.currentTimeMillis() - lastEnvCalculated > 1000 ||
                    (lastEnvPos != null && lastEnvPos.distanceTo(pos) > 1)) {
                CustomSoundEngine.soundPhysicsPlaySoundNew.invoke(null, pos.x(), pos.y(), pos.z(), pointer, !reverbOnly);
                lastEnvPos = pos;
                lastEnvCalculated = System.currentTimeMillis();
            }
        } else if (CustomSoundEngine.soundPhysicsPlaySound != null) {
            if (System.currentTimeMillis() - lastEnvCalculated > 1000 ||
                    (lastEnvPos != null && lastEnvPos.distanceTo(pos) > 1)) {
                CustomSoundEngine.soundPhysicsPlaySound.invoke(null, pos.x(), pos.y(), pos.z(), pointer);
                lastEnvPos = pos;
                lastEnvCalculated = System.currentTimeMillis();
            }
        }

        if (this.getSourceState() != AL10.AL_PLAYING) {
            AL10.alSourcePlay(this.pointer);
            AlUtil.checkErrors("Custom source play");
        }
    }

    private int getSourceState() {
        return !this.playing.get() ? 4116 : AL10.alGetSourcei(this.pointer, 4112);
    }

    public void pause() {
        if (this.getSourceState() == 4114) {
            AL10.alSourcePause(this.pointer);
        }

    }

    public void resume() {
        if (this.getSourceState() == 4115) {
            AL10.alSourcePlay(this.pointer);
        }

    }

    public void stop() {
        if (this.playing.get()) {
            AL10.alSourceStop(this.pointer);
            AlUtil.checkErrors("Stop");
        }

    }

    public boolean isStopped() {
        return this.getSourceState() == 4116;
    }

    public void setPosition(Vec3 vec3d) {
        this.pos = vec3d;
        AL10.alSourcefv(this.pointer, 4100, new float[]{(float)vec3d.x, (float)vec3d.y, (float)vec3d.z});
    }

    public void setDirection(Vec3 vec3d) {
        AL10.alSourcefv(this.pointer, AL10.AL_DIRECTION, new float[]{(float)vec3d.x, (float)vec3d.y, (float)vec3d.z});
    }

    public void setVelocity(Vec3 vec3d) {
        AL10.alSourcefv(this.pointer, AL10.AL_VELOCITY, new float[]{(float)vec3d.x, (float)vec3d.y, (float)vec3d.z});
    }

    public void setAngle(float f) {
        AL10.alSourcef(this.pointer, AL10.AL_CONE_INNER_ANGLE, f);
    }

    public void setConeOuterGain(float f) {
        AL10.alSourcef(this.pointer, AL10.AL_CONE_OUTER_GAIN, f);
    }

    public void setPitch(float f) {
        AL10.alSourcef(this.pointer, 4099, f);
    }

    public void setLooping(boolean bl) {
        AL10.alSourcei(this.pointer, 4103, bl ? 1 : 0);
    }

    public void setMaxVolume(float f) {
        AL10.alSourcef(this.pointer, AL10.AL_MAX_GAIN, f);
    }

    public void setVolume(float f) {
        AL10.alSourcef(this.pointer, AL10.AL_MAX_GAIN, 4.0F);
        AL10.alSourcef(this.pointer, AL10.AL_GAIN, f);
    }

    public void disableAttenuation() {
        AL10.alSourcei(this.pointer, 53248, 0);
    }

    public void setFadeDistance(float f) {
        AL10.alSourcei(this.pointer, AL10.AL_DISTANCE_MODEL, 53252); // 0xD004 old // 53251 default
        AL10.alSourcef(this.pointer, AL10.AL_REFERENCE_DISTANCE, f);
    }

    public void setMaxDistance(float f, float factor) {
        AL10.alSourcef(this.pointer, AL10.AL_MAX_DISTANCE, f);
        AL10.alSourcef(this.pointer, AL10.AL_ROLLOFF_FACTOR, factor);
    }

    public void setRelative(boolean bl) {
        AL10.alSourcei(this.pointer, 514, bl ? 1 : 0);
    }

    private static int getBufferSize(AudioFormat format, int time) {
        return (int)((float)(time * format.getSampleSizeInBits()) / 8.0F * (float)format.getChannels() * format.getSampleRate());
    }

    public void write(byte[] bytes) {
        this.removeProcessedBuffers();

        if (!freeBuffers.isEmpty()) {
            ByteBuffer byteBuffer = MemoryUtil.memAlloc(bytes.length);
            byteBuffer.put(bytes);
            ((Buffer) byteBuffer).flip(); // java 8 support

            int freeBuffer = freeBuffers.poll();

            AL10.alBufferData(freeBuffer, format, byteBuffer, (int)Recorder.getFormat().getSampleRate());
            if (AlUtil.checkErrors("Assigning buffer data")) {
                return;
            }

            AL10.alSourceQueueBuffers(this.pointer, new int[]{freeBuffer});
            this.play();
        }
    }

    public void removeProcessedBuffers() {
        int i = AL10.alGetSourcei(this.pointer, AL10.AL_BUFFERS_PROCESSED);
        while (i > 0) {
            int[] is = new int[1];
            AL10.alSourceUnqueueBuffers(this.pointer, is);
            AlUtil.checkErrors("Unqueue buffers");
            freeBuffers.offer(is[0]);
            i--;
        }
    }
}

