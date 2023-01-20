package su.plo.voice.proto.data.audio.source;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import su.plo.voice.proto.packets.PacketSerializable;
import su.plo.voice.proto.packets.PacketUtil;

import java.io.IOException;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

@AllArgsConstructor
@NoArgsConstructor
@ToString
public class SelfSourceInfo implements PacketSerializable {

    @Getter
    private SourceInfo sourceInfo;
    @Getter
    private UUID playerId;
    @Getter
    private UUID activationId;
    @Getter
    private long sequenceNumber;

    @Override
    public void deserialize(ByteArrayDataInput in) throws IOException {
        this.sourceInfo = SourceInfo.of(in);
        this.playerId = PacketUtil.readUUID(in);
        this.activationId = PacketUtil.readUUID(in);
        this.sequenceNumber = in.readLong();
    }

    @Override
    public void serialize(ByteArrayDataOutput out) throws IOException {
        checkNotNull(sourceInfo).serialize(out);
        PacketUtil.writeUUID(out, playerId);
        PacketUtil.writeUUID(out, activationId);
        out.writeLong(sequenceNumber);
    }
}
