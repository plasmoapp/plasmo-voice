package su.plo.voice.audio.source;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;
import org.lwjgl.system.MemoryUtil;
import su.plo.voice.api.PlasmoVoiceClient;
import su.plo.voice.api.audio.device.AlAudioDevice;
import su.plo.voice.api.audio.device.DeviceException;
import su.plo.voice.api.audio.source.AlSource;
import su.plo.voice.api.event.audio.source.AlSourceClosedEvent;
import su.plo.voice.api.event.audio.source.AlSourceCreatedEvent;
import su.plo.voice.api.event.audio.source.AlSourcePlayEvent;
import su.plo.voice.api.event.audio.source.AlSourceStopEvent;
import su.plo.voice.audio.AlUtil;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public final class AltStreamAlSource extends AlSourceBase {

    private static final Logger LOGGER = LogManager.getLogger(AltStreamAlSource.class);

    private static final int NUM_BUFFERS = 16;

    public static CompletableFuture<AlSource> create(AlAudioDevice device, PlasmoVoiceClient client) {
        CompletableFuture<AlSource> future = new CompletableFuture<>();

        device.runInContext(() -> {
            int[] pointer = new int[1];
            AL11.alGenSources(pointer);

            if (AlUtil.checkErrors("Allocate new source")) {
                future.completeExceptionally(new DeviceException("Failed to allocate new source"));
                return;
            }

            AlSource source = new AltStreamAlSource(client, device, pointer[0]);

            AlSourceCreatedEvent event = new AlSourceCreatedEvent(source);
            client.getEventBus().call(event);

            future.complete(source);
        });

        return future;
    }

    private final LinkedBlockingQueue<ByteBuffer> queue = new LinkedBlockingQueue<>();
    private final AtomicBoolean isStreaming = new AtomicBoolean(false);

    private final LinkedList<Integer> freeBuffers = new LinkedList<>();

    private AltStreamAlSource(PlasmoVoiceClient client, AlAudioDevice device, int pointer) {
        super(client, device, pointer);

        int[] buffers = new int[NUM_BUFFERS];
        AL10.alGenBuffers(buffers);
        AlUtil.checkErrors("Creating buffers");

        for (int buffer : buffers) {
            freeBuffers.offer(buffer);
        }
    }

    @Override
    public void play() {
        AlUtil.checkDeviceContext(device);

        AlSourcePlayEvent event = new AlSourcePlayEvent(this);
        client.getEventBus().call(event);
        if (event.isCancelled()) return;

        boolean isStreaming = this.isStreaming.get();
        State state = getState();
        if (isStreaming && state == State.PAUSED) {
            AL11.alSourcePlay(pointer);
            AlUtil.checkErrors("Source play");
            return;
        } else if (isStreaming) {
            return;
        }

//        startStreamThread();
    }

    @Override
    public void stop() {
        AlUtil.checkDeviceContext(device);

        AlSourceStopEvent event = new AlSourceStopEvent(this);
        client.getEventBus().call(event);
        if (event.isCancelled()) return;

        isStreaming.set(false);

//        try {
//            thread.join();
//        } catch (InterruptedException ignored) {
//        }
    }

    @Override
    public void write(byte[] samples) {
        // set play state before queue
        if (this.getState() != State.PLAYING) {
            AL10.alSourcePlay(this.pointer);
            AlUtil.checkErrors("Custom source play");
        }

        this.removeProcessedBuffers();
        this.bufferData(samples);
//        ByteBuffer buffer = MemoryUtil.memAlloc(samples.length);
//        buffer.put(samples);
//        ((Buffer) buffer).flip();
//
//        AlSourceWriteEvent event = new AlSourceWriteEvent(this, buffer);
//        client.getEventBus().call(event);
//        if (event.isCancelled()) return;
//
//        queue.offer(buffer);
    }

    private void bufferData(byte[] bytes) {
        ByteBuffer byteBuffer = MemoryUtil.memAlloc(bytes.length);
        byteBuffer.put(bytes);
        ((Buffer) byteBuffer).flip(); // java 8 support

        Integer freeBuffer = freeBuffers.poll();
        if (freeBuffer == null) {
            while (freeBuffer == null) {
                this.removeProcessedBuffers();
                freeBuffer = freeBuffers.poll();
            }
        }

        AL10.alBufferData(freeBuffer, format, byteBuffer, (int) device.getFormat().get().getSampleRate());
        if (AlUtil.checkErrors("Assigning buffer data to " + freeBuffer + " with format " + format + " and rate " + device.getFormat().get().getSampleRate())) {
            return;
        }

        AL10.alSourceQueueBuffers(this.pointer, new int[]{freeBuffer});
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

    @Override
    public CompletableFuture<Void> close() {
        CompletableFuture<Void> future = new CompletableFuture<>();

        device.runInContext(() -> {
            stop();

            this.removeProcessedBuffers();
            while (!freeBuffers.isEmpty()) {
                AL10.alDeleteBuffers(freeBuffers.poll());
            }

            AL11.alDeleteSources(new int[]{ pointer });
            AlUtil.checkErrors("Cleanup");

            AlSourceClosedEvent event = new AlSourceClosedEvent(this);
            client.getEventBus().call(event);

            future.complete(null);
        });

        return future;
    }
}
