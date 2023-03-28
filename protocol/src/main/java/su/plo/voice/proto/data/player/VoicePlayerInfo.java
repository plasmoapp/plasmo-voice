package su.plo.voice.proto.data.player;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.proto.packets.PacketSerializable;
import su.plo.voice.proto.packets.PacketUtil;

import java.util.Collections;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class VoicePlayerInfo implements PacketSerializable {

    private UUID playerId;
    private String playerNick;
    private boolean muted;
    private boolean voiceDisabled;
    private boolean microphoneMuted;

    @Override
    public void deserialize(ByteArrayDataInput in) {
        this.playerId = PacketUtil.readUUID(in);
        this.playerNick = in.readUTF();
        this.muted = in.readBoolean();
        this.voiceDisabled = in.readBoolean();
        this.microphoneMuted = in.readBoolean();
    }

    @Override
    public void serialize(ByteArrayDataOutput out) {
        PacketUtil.writeUUID(out, checkNotNull(playerId));
        out.writeUTF(checkNotNull(playerNick));
        out.writeBoolean(muted);
        out.writeBoolean(voiceDisabled);
        out.writeBoolean(microphoneMuted);
    }

    public @NotNull MinecraftGameProfile toGameProfile() {
        return new MinecraftGameProfile(
                playerId,
                playerNick,
                Collections.emptyList()
        );
    }
}
