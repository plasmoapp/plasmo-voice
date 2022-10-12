package su.plo.voice.proto.data.audio.source;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.proto.data.player.VoicePlayerInfo;

import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

@NoArgsConstructor
@ToString(callSuper = true)
public final class PlayerSourceInfo extends SourceInfo {

    @Getter
    private VoicePlayerInfo playerInfo;

    public PlayerSourceInfo(@NotNull String addonId,
                            @NotNull UUID sourceId,
                            @NotNull UUID lineId,
                            byte state,
                            @NotNull String codec,
                            boolean stereo,
                            boolean iconVisible,
                            int angle,
                            VoicePlayerInfo playerInfo) {
        super(addonId, sourceId, lineId, state, codec, stereo, iconVisible, angle);
        this.playerInfo = playerInfo;
    }

    @Override
    public void deserialize(ByteArrayDataInput in) {
        super.deserialize(in);

        this.playerInfo = new VoicePlayerInfo();
        playerInfo.deserialize(in);
    }

    @Override
    public void serialize(ByteArrayDataOutput out) {
        super.serialize(out);

        checkNotNull(playerInfo, "playerInfo").serialize(out);
    }

    @Override
    public Type getType() {
        return Type.PLAYER;
    }
}
