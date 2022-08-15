package su.plo.voice.client.audio.device.source;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.openal.AL11;
import org.lwjgl.openal.EXTThreadLocalContext;
import org.lwjgl.system.MemoryUtil;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.audio.device.AlAudioDevice;
import su.plo.voice.api.client.audio.device.DeviceException;
import su.plo.voice.api.client.audio.device.source.AlSource;
import su.plo.voice.api.client.event.audio.source.*;
import su.plo.voice.client.audio.AlUtil;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public final class StreamAlSource extends AlSourceBase {

    private static final Logger LOGGER = LogManager.getLogger(StreamAlSource.class);
    private static final int DEFAULT_NUM_BUFFERS = 8;

    public static CompletableFuture<AlSource> create(AlAudioDevice device, PlasmoVoiceClient client, int numBuffers) {
        CompletableFuture<AlSource> future = new CompletableFuture<>();

        device.runInContext(() -> {
            int[] pointer = new int[1];
            AL11.alGenSources(pointer);

            if (AlUtil.checkErrors("Allocate new source")) {
                future.completeExceptionally(new DeviceException("Failed to allocate new source"));
                return;
            }

            AlSource source = new StreamAlSource(client, device, numBuffers, pointer[0]);

            AlSourceCreatedEvent event = new AlSourceCreatedEvent(source);
            client.getEventBus().call(event);

            future.complete(source);
        });

        return future;
    }

    private final int numBuffers;
    private final LinkedBlockingQueue<ByteBuffer> queue = new LinkedBlockingQueue<>();
    private final AtomicBoolean isStreaming = new AtomicBoolean(false);

    private Thread thread;
    private int[] buffers;

    private StreamAlSource(PlasmoVoiceClient client, AlAudioDevice device, int numBuffers, int pointer) {
        super(client, device, pointer);
        this.numBuffers = numBuffers == 0 ? DEFAULT_NUM_BUFFERS : numBuffers;
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
        } else if (thread != null && !thread.isAlive()) {
            stop();
        }

        startStreamThread();
    }

    @Override
    public void stop() {
        AlUtil.checkDeviceContext(device);

        AlSourceStopEvent event = new AlSourceStopEvent(this);
        client.getEventBus().call(event);
        if (event.isCancelled()) return;

        isStreaming.set(false);

        try {
            thread.join();
        } catch (InterruptedException ignored) {
        }

        queue.clear();
    }

    @Override
    public void write(byte[] samples) {
        ByteBuffer buffer = MemoryUtil.memAlloc(samples.length);
        buffer.put(samples);
        ((Buffer) buffer).flip();

        AlSourceWriteEvent event = new AlSourceWriteEvent(this, buffer);
        client.getEventBus().call(event);
        if (event.isCancelled()) return;

        queue.offer(buffer);

        if (queue.size() > 1_000) {
            LOGGER.warn("Queue overflow, stopping stream");
            stop();
        }
    }

    @Override
    public CompletableFuture<Void> close() {
        CompletableFuture<Void> future = new CompletableFuture<>();

        device.runInContext(() -> {
            stop();

            AL11.alDeleteBuffers(buffers);

            AL11.alDeleteSources(new int[]{ pointer });
            AlUtil.checkErrors("Cleanup");

            AlSourceClosedEvent event = new AlSourceClosedEvent(this);
            client.getEventBus().call(event);

            future.complete(null);
        });

        return future;
    }

    private void startStreamThread() {
        isStreaming.set(true);

        this.thread = new Thread(this::stream);
        thread.setName("AL Source Stream");
        thread.setDaemon(false);

        thread.start();
    }

    private void stream() {
        EXTThreadLocalContext.alcSetThreadContext(device.getContextPointer().get());

        this.buffers = new int[numBuffers];
        AL11.alGenBuffers(buffers);

        AL11.alSourcePlay(pointer);
        AlUtil.checkErrors("Source play");

        if (!fillQueue()) {
            throw new IllegalStateException("Failed to fill initial queue");
        }

        while (isStreaming.get()) {
            if (getState() == State.STOPPED) {
                AL11.alSourcePlay(pointer);
                AlUtil.checkErrors("Source play");
            }

            int processedBuffers = getInt(AL11.AL_BUFFERS_PROCESSED);
            AlUtil.checkErrors("Get processed buffers");

            while (processedBuffers > 0) {
                int[] buffer = new int[1];
                AL11.alSourceUnqueueBuffers(this.pointer, buffer);
                AlUtil.checkErrors("Unqueue buffer");

                // Bits can be 0 if the format or parameters are corrupt, avoid division by zero
                int bits = AL11.alGetBufferi(buffer[0], AL11.AL_BITS);
                if (bits == 0) {
                    LOGGER.warn("Corrupted stream");
                    continue;
                }

                if (!fillAndPushBuffer(buffer[0])) {
                    processedBuffers--;
                }
            }

            try {
                Thread.sleep(10L);
            } catch (InterruptedException e) {
                break;
            }
        }

        EXTThreadLocalContext.alcSetThreadContext(0L);
    }

    private boolean fillQueue() {
        for (int i = 0; (i < numBuffers); ++i) {
            if (getState() == State.STOPPED) {
                AL11.alSourcePlay(pointer);
                AlUtil.checkErrors("Source play");
            }

            if (fillAndPushBuffer(buffers[i])) {
                return false;
            }
        }

        return true;
    }

    private boolean fillAndPushBuffer(int buffer) {
        ByteBuffer byteBuffer;
        try {
            byteBuffer = queue.poll(10L, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {
            System.out.println("queue is empty");
            return true;
        }

        AL11.alBufferData(buffer, format, byteBuffer, (int) device.getFormat().get().getSampleRate());
        if (AlUtil.checkErrors("Assigning buffer data")) return true;

        AL11.alSourceQueueBuffers(this.pointer, new int[]{ buffer });
        return false;
    }
}
