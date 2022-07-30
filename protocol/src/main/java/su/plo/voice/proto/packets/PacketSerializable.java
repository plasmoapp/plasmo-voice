package su.plo.voice.proto.packets;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

public interface PacketSerializable {

    void deserialize(ByteArrayDataInput in);

    void serialize(ByteArrayDataOutput out);
}
