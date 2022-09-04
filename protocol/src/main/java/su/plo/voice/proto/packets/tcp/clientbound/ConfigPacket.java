package su.plo.voice.proto.packets.tcp.clientbound;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.proto.data.capture.VoiceActivation;
import su.plo.voice.proto.packets.PacketUtil;

import java.io.IOException;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;

@NoArgsConstructor
@ToString
public final class ConfigPacket extends ConfigPlayerInfoPacket {

    @Getter
    private UUID serverId;

    @Getter
    private int sampleRate;

    @Getter
    private String codec; // todo: per-source codecs

    @Getter
    private int fadeDivisor;

    @Getter
    private VoiceActivation proximityActivation;

    private List<VoiceActivation> activations;

    public ConfigPacket(@NotNull UUID serverId,
                        int sampleRate,
                        @NotNull String codec,
                        int fadeDivisor,
                        @NotNull VoiceActivation proximityActivation,
                        @NotNull List<VoiceActivation> activations,
                        @NotNull Map<String, Boolean> permissions) {
        super(permissions);
        this.serverId = serverId;
        this.sampleRate = sampleRate;
        this.codec = codec;
        this.fadeDivisor = fadeDivisor;
        this.proximityActivation = proximityActivation;
        this.activations = activations;
    }

    public Collection<VoiceActivation> getActivations() {
        return activations;
    }

    @Override
    public void read(ByteArrayDataInput in) throws IOException {
        this.serverId = PacketUtil.readUUID(in);
        this.sampleRate = in.readInt();
        this.codec = PacketUtil.readNullableString(in);
        this.fadeDivisor = in.readInt();

        // activations
        this.proximityActivation = new VoiceActivation();
        proximityActivation.deserialize(in);

        this.activations = new ArrayList<>();
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            VoiceActivation activation = new VoiceActivation();
            activation.deserialize(in);
        }
    }

    @Override
    public void write(ByteArrayDataOutput out) throws IOException {
        PacketUtil.writeUUID(out, serverId);
        out.writeInt(sampleRate);
        PacketUtil.writeNullableString(out, codec);
        out.writeInt(fadeDivisor);

        // activations
        checkNotNull(proximityActivation).serialize(out);

        out.writeInt(activations.size());
        activations.forEach(activation -> activation.serialize(out));
    }

    @Override
    public void handle(ClientPacketTcpHandler handler) {
        handler.handle(this);
    }
}
