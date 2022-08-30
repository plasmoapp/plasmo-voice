package su.plo.voice.proto.packets;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import org.jetbrains.annotations.ApiStatus;

public interface PacketSerializable {

    @ApiStatus.Internal
    void deserialize(ByteArrayDataInput in);

    @ApiStatus.Internal
    void serialize(ByteArrayDataOutput out);
}
