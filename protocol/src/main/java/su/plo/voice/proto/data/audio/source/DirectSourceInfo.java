package su.plo.voice.proto.data.audio.source;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.proto.data.player.MinecraftGameProfile;
import su.plo.voice.proto.data.pos.Pos3d;

import java.io.IOException;
import java.util.UUID;

@NoArgsConstructor
@ToString(callSuper = true)
public final class DirectSourceInfo extends SourceInfo {

    @Getter
    private @Nullable MinecraftGameProfile sender;
    @Getter
    private @Nullable Pos3d relativePosition;
    @Getter
    private @Nullable Pos3d lookAngle;
    @Getter
    private boolean cameraRelative = true;

    public DirectSourceInfo(@NotNull String addonId,
                            @NotNull UUID sourceId,
                            @NotNull UUID lineId,
                            @Nullable String sourceName,
                            byte state,
                            @NotNull String codec,
                            boolean stereo,
                            boolean iconVisible,
                            int angle,
                            @Nullable MinecraftGameProfile sender,
                            @Nullable Pos3d relativePosition,
                            @Nullable Pos3d lookAngle,
                            boolean cameraRelative) {
        super(addonId, sourceId, lineId, sourceName, state, codec, stereo, iconVisible, angle);

        this.sender = sender;
        this.relativePosition = relativePosition;
        this.lookAngle = lookAngle;
        this.cameraRelative = cameraRelative;
    }

    @Override
    public void deserialize(ByteArrayDataInput in) throws IOException {
        super.deserialize(in);

        if (in.readBoolean()) {
            this.sender = new MinecraftGameProfile();
            sender.deserialize(in);
        }

        if (in.readBoolean()) {
            this.relativePosition = new Pos3d();
            relativePosition.deserialize(in);
        }

        this.cameraRelative = in.readBoolean();
    }

    @Override
    public void serialize(ByteArrayDataOutput out) throws IOException {
        super.serialize(out);

        out.writeBoolean(sender != null);
        if (sender != null) sender.serialize(out);

        out.writeBoolean(relativePosition != null);
        if (relativePosition != null) relativePosition.serialize(out);

        out.writeBoolean(cameraRelative);
    }

    @Override
    public Type getType() {
        return Type.DIRECT;
    }
}
