package su.plo.voice.client.sound;

import net.minecraft.client.Minecraft;
import su.plo.voice.client.socket.SocketClientUDPQueue;
import su.plo.voice.client.sound.openal.CustomSource;
import su.plo.voice.client.sound.opus.OpusDecoder;
import su.plo.voice.common.packets.udp.VoiceServerPacket;

import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class AbstractSoundQueue extends Thread {
    public CustomSource source;
    protected final Minecraft minecraft = Minecraft.getInstance();
    public BlockingQueue<VoiceServerPacket> queue = new LinkedBlockingQueue<>();
    protected boolean stopped;
    protected long lastPacketTime;
    public final UUID from;
    public long lastSequenceNumber;

    protected OpusDecoder opusDecoder;
    protected double lastOcclusion = -1;

    protected final Compressor compressor = new Compressor();

    public AbstractSoundQueue(UUID from) {
        this.from = from;
        this.lastPacketTime = System.currentTimeMillis() - 300L;
        this.lastSequenceNumber = -1L;
        this.opusDecoder = new OpusDecoder(Recorder.getSampleRate(), Recorder.getFrameSize(), Recorder.getMtuSize());
    }

    public boolean canKill() {
        return System.currentTimeMillis() - lastPacketTime > 30_000L;
    }

    public void closeAndKill() {
        stopped = true;
        SocketClientUDPQueue.talking.remove(this.from);

        this.opusDecoder.close();
    }

    public boolean isClosed() {
        return stopped;
    }

    public void addQueue(VoiceServerPacket packet) {
        if(packet == null) {
            return;
        }

        this.queue.add(packet);
    }
}
