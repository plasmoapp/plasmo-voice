package su.plo.voice.proto.packets;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import org.jetbrains.annotations.ApiStatus;

import java.io.IOException;

/**
 * The PacketSerializable interface defines the contract for objects that can be serialized
 * and deserialized to and from byte streams using a {@link ByteArrayDataInput} for deserialization
 * and a {@link ByteArrayDataOutput} for serialization. Implementing classes must provide
 * methods to serialize themselves into a byte stream and deserialize themselves from a byte stream.
 */
public interface PacketSerializable {

    /**
     * Deserialize the object from a byte stream using the provided {@link ByteArrayDataInput}.
     * This method reconstructs the object from its serialized form.
     *
     * @param in The {@link ByteArrayDataInput} containing the serialized data.
     * @throws IOException If an I/O error occurs during deserialization.
     */
    @ApiStatus.Internal
    void deserialize(ByteArrayDataInput in) throws IOException;

    /**
     * Serialize the object to a byte stream using the provided {@link ByteArrayDataOutput}.
     * This method converts the object into its serialized form.
     *
     * @param out The {@link ByteArrayDataOutput} to which the serialized data should be written.
     * @throws IOException If an I/O error occurs during serialization.
     */
    @ApiStatus.Internal
    void serialize(ByteArrayDataOutput out) throws IOException;
}
