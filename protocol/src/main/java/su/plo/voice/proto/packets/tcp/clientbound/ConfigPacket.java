package su.plo.voice.proto.packets.tcp.clientbound;

import com.google.common.collect.Lists;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.proto.data.audio.capture.VoiceActivation;
import su.plo.voice.proto.data.audio.line.VoiceSourceLine;
import su.plo.voice.proto.packets.PacketUtil;

import java.io.IOException;
import java.util.*;

@NoArgsConstructor
@ToString
public final class ConfigPacket extends ConfigPlayerInfoPacket {

    @Getter
    private UUID serverId;
    @Getter
    private int sampleRate;
    @Getter
    private String codec; // todo: per-source codecs
    private List<VoiceSourceLine> sourceLines;
    private List<VoiceActivation> activations;

    public ConfigPacket(@NotNull UUID serverId,
                        int sampleRate,
                        @NotNull String codec,
                        @NotNull List<VoiceSourceLine> sourceLines,
                        @NotNull List<VoiceActivation> activations,
                        @NotNull Map<String, Boolean> permissions) {
        super(permissions);

        this.serverId = serverId;
        this.sampleRate = sampleRate;
        this.codec = codec;
        this.sourceLines = sourceLines;
        this.activations = activations;
    }

    public Collection<VoiceSourceLine> getSourceLines() {
        return sourceLines;
    }

    public Collection<VoiceActivation> getActivations() {
        return activations;
    }

    @Override
    public void read(ByteArrayDataInput in) throws IOException {
        this.serverId = PacketUtil.readUUID(in);
        this.sampleRate = in.readInt();
        this.codec = PacketUtil.readNullableString(in);

        // source lines
        this.sourceLines = Lists.newArrayList();
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            VoiceSourceLine sourceLine = new VoiceSourceLine();
            sourceLine.deserialize(in);
            sourceLines.add(sourceLine);
        }

        // activations
        this.activations = new ArrayList<>();
        size = in.readInt();
        for (int i = 0; i < size; i++) {
            VoiceActivation activation = new VoiceActivation();
            activation.deserialize(in);
            activations.add(activation);
        }
    }

    @Override
    public void write(ByteArrayDataOutput out) throws IOException {
        PacketUtil.writeUUID(out, serverId);
        out.writeInt(sampleRate);
        PacketUtil.writeNullableString(out, codec);

        // source lines
        out.writeInt(sourceLines.size());
        sourceLines.forEach(sourceLine -> sourceLine.serialize(out));

        // activations
        out.writeInt(activations.size());
        activations.forEach(activation -> activation.serialize(out));
    }

    @Override
    public void handle(ClientPacketTcpHandler handler) {
        handler.handle(this);
    }
}
