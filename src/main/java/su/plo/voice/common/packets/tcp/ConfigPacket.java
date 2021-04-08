package su.plo.voice.common.packets.tcp;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import su.plo.voice.common.packets.Packet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ConfigPacket implements Packet {
    private int sampleRate;
    private final List<Integer> distances;
    private int defaultDistance;
    private int maxPriorityDistance;
    private int fadeDivisor;
    private int priorityFadeDivisor;
    private boolean disableVoiceActivation;

    public ConfigPacket() {
        this.distances = new ArrayList<>();
    }

    public ConfigPacket(int sampleRate, List<Integer> distances, int defaultDistance, int maxPriorityDistance,
                        boolean disableVoiceActivation, int fadeDivisor, int priorityFadeDivisor) {
        this.sampleRate = sampleRate;
        this.distances = distances;
        this.defaultDistance = defaultDistance;
        this.maxPriorityDistance = maxPriorityDistance;
        this.fadeDivisor = fadeDivisor;
        this.priorityFadeDivisor = priorityFadeDivisor;
        this.disableVoiceActivation = disableVoiceActivation;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public List<Integer> getDistances() {
        return distances;
    }

    public int getDefaultDistance() {
        return defaultDistance;
    }

    public int getFadeDivisor() {
        return fadeDivisor;
    }

    public int getPriorityFadeDivisor() {
        return priorityFadeDivisor;
    }

    public int getMaxPriorityDistance() {
        return maxPriorityDistance;
    }

    public boolean isDisableVoiceActivation() {
        return disableVoiceActivation;
    }

    @Override
    public void write(ByteArrayDataOutput buf) throws IOException {
        buf.writeInt(sampleRate);
        buf.writeInt(distances.size());
        for(int distance : distances) {
            buf.writeInt(distance);
        }
        buf.writeInt(defaultDistance);
        buf.writeInt(maxPriorityDistance);
        buf.writeInt(fadeDivisor);
        buf.writeInt(priorityFadeDivisor);
        buf.writeBoolean(disableVoiceActivation);
    }

    @Override
    public void read(ByteArrayDataInput buf) throws IOException {
        sampleRate = buf.readInt();
        int length = buf.readInt();
        for(int i = 0; i < length; i++) {
            distances.add(buf.readInt());
        }
        defaultDistance = buf.readInt();
        maxPriorityDistance = buf.readInt();
        fadeDivisor = buf.readInt();
        priorityFadeDivisor = buf.readInt();
        disableVoiceActivation = buf.readBoolean();
    }
}
