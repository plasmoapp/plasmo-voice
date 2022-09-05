package su.plo.voice.proto.data.source;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.proto.data.capture.VoiceActivation;
import su.plo.voice.proto.data.pos.Pos3d;
import su.plo.voice.proto.packets.PacketUtil;

import java.util.UUID;

@NoArgsConstructor
@ToString(callSuper = true)
public final class DirectSourceInfo extends SourceInfo {

    @Getter
    private @Nullable UUID senderId;

    @Getter
    private @Nullable Pos3d relativePosition;

    @Getter
    private @Nullable Pos3d lookAngle;

    @Getter
    private boolean cameraRelative = true;

    public DirectSourceInfo(@NotNull String addonId,
                            @NotNull UUID sourceId,
                            byte state,
                            @NotNull String codec,
                            boolean iconVisible,
                            int angle,
                            @Nullable UUID senderId,
                            @Nullable Pos3d relativePosition,
                            @Nullable Pos3d lookAngle,
                            boolean cameraRelative) {
        super(addonId, sourceId, state, codec, VoiceActivation.PROXIMITY_ID, iconVisible, angle);

        this.senderId = senderId;
        this.relativePosition = relativePosition;
        this.lookAngle = lookAngle;
        this.cameraRelative = cameraRelative;
    }

    @Override
    public void deserialize(ByteArrayDataInput in) {
        super.deserialize(in);

        if (in.readBoolean()) this.senderId = PacketUtil.readUUID(in);

        if (in.readBoolean()) {
            this.relativePosition = new Pos3d();
            relativePosition.deserialize(in);
        }

        this.cameraRelative = in.readBoolean();
    }

    @Override
    public void serialize(ByteArrayDataOutput out) {
        super.serialize(out);

        out.writeBoolean(senderId != null);
        if (senderId != null) PacketUtil.writeUUID(out, senderId);

        out.writeBoolean(relativePosition != null);
        if (relativePosition != null) relativePosition.serialize(out);

        out.writeBoolean(cameraRelative);
    }

    @Override
    public Type getType() {
        return Type.DIRECT;
    }
}
