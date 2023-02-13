package su.plo.voice.client.audio.device.source;

import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.openal.AL11;
import org.lwjgl.openal.EXTThreadLocalContext;
import org.lwjgl.system.MemoryUtil;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.audio.device.AlAudioDevice;
import su.plo.voice.api.client.audio.device.DeviceException;
import su.plo.voice.api.client.audio.device.source.AlSource;
import su.plo.voice.api.client.event.audio.device.source.*;
import su.plo.voice.client.audio.AlUtil;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public final class StreamAlSource extends BaseAlSource {

    private static final Logger LOGGER = LogManager.getLogger(StreamAlSource.class);
    private static final int DEFAULT_NUM_BUFFERS = 8;

    @Setter
    private long closeTimeoutMs = 25_000L;

    public static AlSource create(AlAudioDevice device, PlasmoVoiceClient client, boolean stereo, int numBuffers) {
        CompletableFuture<AlSource> future = new CompletableFuture<>();

        device.runInContext(() -> {
            int[] pointer = new int[1];
            AL11.alGenSources(pointer);

            if (AlUtil.checkErrors("Allocate new source")) {
                future.completeExceptionally(new DeviceException("Failed to allocate new source"));
                return;
            }

            AlSource source = new StreamAlSource(client, device, stereo, numBuffers, pointer[0]);

            AlSourceCreatedEvent event = new AlSourceCreatedEvent(source);
            client.getEventBus().call(event);

            future.complete(source);
        });

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private final int numBuffers;
    private final LinkedBlockingQueue<ByteBuffer> queue = new LinkedBlockingQueue<>();
    private final AtomicBoolean isStreaming = new AtomicBoolean(false);
    private final byte[] emptyBuffer;

    private Thread thread;
    private int[] buffers;
    private int[] availableBuffer = new int[1];
    private AtomicBoolean emptyFilled = new AtomicBoolean(false);
    private long lastBufferTime;

    private StreamAlSource(PlasmoVoiceClient client, AlAudioDevice device, boolean stereo, int numBuffers, int pointer) {
        super(client, device, stereo, pointer);
        this.numBuffers = numBuffers == 0 ? DEFAULT_NUM_BUFFERS : numBuffers;
        this.emptyBuffer = new byte[device.getBufferSize()];
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

        AL11.alSourceStop(pointer);
        AlUtil.checkErrors("Source stop");

        isStreaming.set(false);

        try {
            thread.interrupt();
            thread.join();
        } catch (InterruptedException ignored) {
        }

        queue.clear();
    }

    @Override
    public void write(byte[] samples) {
        if (!isStreaming.get()) return;

        if (samples == null) { // fill queue with empty buffers
            for (int i = 0; i < numBuffers; i++) {
                write(emptyBuffer);
            }
            return;
        } else if (samples.length == 0) {
            write(emptyBuffer);
            return;
        }

        ByteBuffer buffer = MemoryUtil.memAlloc(samples.length);
        buffer.put(samples);
        ((Buffer) buffer).flip();

        AlSourceWriteEvent event = new AlSourceWriteEvent(this, buffer);
        client.getEventBus().call(event);
        if (event.isCancelled()) return;

        queue.offer(buffer);
        if (samples != emptyBuffer) {
            this.emptyFilled.set(false);
            this.lastBufferTime = System.currentTimeMillis();
        }

        if (queue.size() > 1_000) {
            LOGGER.warn("Queue overflow, stopping stream");
            stop();
        }
    }

    @Override
    public void close() {
        if (!isStreaming.get()) return;

        device.runInContext(() -> {
            stop();

            AlSourceClosedEvent event = new AlSourceClosedEvent(this);
            client.getEventBus().call(event);

            removeProcessedBuffers();

            AL11.alDeleteBuffers(buffers);
            AlUtil.checkErrors("Delete buffers");

            AL11.alDeleteSources(new int[]{pointer});
            AlUtil.checkErrors("Delete source");

            this.pointer = 0;
        });
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
        AlUtil.checkErrors("Source gen buffers");

        queueWithEmptyBuffers();
        fillQueue();

        this.lastBufferTime = System.currentTimeMillis();
        this.availableBuffer[0] = -1;

        while (isStreaming.get()) {
            int queueSize = queue.size();

            int processedBuffers = getInt(AL11.AL_BUFFERS_PROCESSED);
            AlUtil.checkErrors("Get processed buffers");

            while (processedBuffers > 0 || availableBuffer[0] != -1) {
                if (availableBuffer[0] == -1) {
                    AL11.alSourceUnqueueBuffers(pointer, availableBuffer);
                    AlUtil.checkErrors("Unqueue buffer");

                    // Bits can be 0 if the format or parameters are corrupt, avoid division by zero
                    int bits = AL11.alGetBufferi(availableBuffer[0], AL11.AL_BITS);
                    AlUtil.checkErrors("Source get buffer int");
                    if (bits == 0) {
                        LOGGER.warn("Corrupted stream");
                        continue;
                    }

                    if (availableBuffer[0] != -1) {
                        AlSourceBufferUnqueuedEvent unqueuedEvent = new AlSourceBufferUnqueuedEvent(this, availableBuffer[0]);
                        client.getEventBus().call(unqueuedEvent);
                    }
                }

                if (availableBuffer[0] != -1 && fillAndPushBuffer(availableBuffer[0])) {
                    availableBuffer[0] = -1;
                    processedBuffers--;
                } else {
                    break;
                }
            }

            State state = getState();
            if (state == State.STOPPED && queueSize == 0 && !emptyFilled.get()) {
                removeProcessedBuffers();
                availableBuffer[0] = -1;

                queueWithEmptyBuffers();
                fillQueue();

                client.getEventBus().call(new AlStreamSourceStoppedEvent(this));

                play();
                AL11.alSourcePlay(pointer);
                AlUtil.checkErrors("Source play");
            } else if (state != State.PLAYING && state != State.PAUSED && queueSize > 0) {
                AL11.alSourcePlay(pointer);
                AlUtil.checkErrors("Source play");
            }

            if (closeTimeoutMs > 0L && System.currentTimeMillis() - lastBufferTime > closeTimeoutMs) {
                LOGGER.info("Stream timed out. Closing...");
                close();
                break;
            }

            try {
                Thread.sleep(5L);
            } catch (InterruptedException e) {
                break;
            }
        }

        EXTThreadLocalContext.alcSetThreadContext(0L);
    }

    private void queueWithEmptyBuffers() {
        for (int i = 0; i < numBuffers; ++i) {
            write(emptyBuffer);
        }
        this.emptyFilled.set(true);
    }

    private void fillQueue() {
        for (int i = 0; i < numBuffers; ++i) {
            fillAndPushBuffer(buffers[i]);
        }
    }

    private boolean fillAndPushBuffer(int buffer) {
        ByteBuffer byteBuffer = queue.poll();
        if (byteBuffer == null) return false;

        AL11.alBufferData(buffer, format, byteBuffer, (int) device.getFormat().get().getSampleRate());
        if (AlUtil.checkErrors("Assigning buffer data")) return false;

        AL11.alSourceQueueBuffers(pointer, new int[]{buffer});
        if (AlUtil.checkErrors("Queue buffer data")) return false;

        AlSourceBufferQueuedEvent event = new AlSourceBufferQueuedEvent(this, byteBuffer, buffer);
        client.getEventBus().call(event);

        return true;
    }

    private void removeProcessedBuffers() {
        int processedBuffers = getInt(AL11.AL_BUFFERS_PROCESSED);
        AlUtil.checkErrors("Get processed buffers");

        while (processedBuffers > 0) {
            int[] buffer = new int[1];
            AL11.alSourceUnqueueBuffers(pointer, buffer);
            AlUtil.checkErrors("Unqueue buffer");
            processedBuffers--;
        }
    }
}
