package su.plo.voice.proto.data.source;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.proto.data.VoicePlayerInfo;

import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

@NoArgsConstructor
@ToString
public class PlayerSourceInfo extends SourceInfo {

    @Getter
    private VoicePlayerInfo playerInfo;

    public PlayerSourceInfo(@NotNull UUID sourceId, @NotNull String codec, boolean iconVisible, VoicePlayerInfo playerInfo) {
        super(sourceId, codec, iconVisible);
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
