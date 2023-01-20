package su.plo.voice.proto.packets;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import org.jetbrains.annotations.ApiStatus;

import java.io.IOException;

public interface PacketSerializable {

    @ApiStatus.Internal
    void deserialize(ByteArrayDataInput in) throws IOException;

    @ApiStatus.Internal
    void serialize(ByteArrayDataOutput out) throws IOException;
}
