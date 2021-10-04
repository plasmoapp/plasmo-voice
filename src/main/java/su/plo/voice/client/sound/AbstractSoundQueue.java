package su.plo.voice.client.sound;

import net.minecraft.client.Minecraft;
import su.plo.voice.client.sound.openal.CustomSource;
import su.plo.voice.client.sound.opus.OpusDecoder;
import su.plo.voice.common.packets.udp.VoiceServerPacket;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public abstract class AbstractSoundQueue extends Thread {
    private final static int packetThreshold = 3;

    public CustomSource source;
    protected final Minecraft minecraft = Minecraft.getInstance();
    public BlockingQueue<VoiceServerPacket> queue = new LinkedBlockingQueue<>();
    public List<VoiceServerPacket> buffer = new ArrayList<>();
    protected boolean stopped;
    protected long lastPacketTime;
    public final UUID from;
    public long lastSequenceNumber;

    protected final OpusDecoder opusDecoder;
    protected double lastOcclusion = -1;

    public AbstractSoundQueue(UUID from) {
        this.from = from;
        this.lastPacketTime = System.currentTimeMillis() - 300L;
        this.lastSequenceNumber = -1L;
        this.opusDecoder = new OpusDecoder(Recorder.getSampleRate(), Recorder.getFrameSize(), Recorder.getMtuSize());
    }

    public boolean canKill() {
        return System.currentTimeMillis() - lastPacketTime > 30_000L;
    }

    public boolean canClose() {
        return System.currentTimeMillis() - lastPacketTime > 1000L;
    }

    public void closeAndKill() {
        stopped = true;

        this.opusDecoder.close();
    }

    public boolean isClosed() {
        return stopped;
    }

    public void addQueue(VoiceServerPacket packet) {
        if(packet == null) {
            return;
        }

        this.queue.offer(packet);

        synchronized (this) {
            this.notify();
        }
    }

    protected VoiceServerPacket pollQueue() throws InterruptedException {
        VoiceServerPacket packet = queue.poll(10, TimeUnit.MILLISECONDS);
        if (packet == null) {
            return pollBuffer();
        }

        if (lastSequenceNumber + 1 == packet.getSequenceNumber()) {
            return packet;
        }

        buffer.add(packet);
        buffer.sort(Comparator.comparingLong(VoiceServerPacket::getSequenceNumber));
        return pollBuffer();
    }

    private VoiceServerPacket pollBuffer() {
        return buffer.size() > packetThreshold
                ? buffer.remove(0)
                : null;
    }
}
