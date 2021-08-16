package su.plo.voice.common.packets.tcp;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.AllArgsConstructor;
import lombok.Getter;
import su.plo.voice.common.packets.Packet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class ConfigPacket implements Packet {
    @Getter
    private int sampleRate;
    @Getter
    private final List<Integer> distances;
    @Getter
    private int defaultDistance;
    @Getter
    private int maxPriorityDistance;
    @Getter
    private int fadeDivisor;
    @Getter
    private int priorityFadeDivisor;
    @Getter
    private boolean disableVoiceActivation;

    public ConfigPacket() {
        this.distances = new ArrayList<>();
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
