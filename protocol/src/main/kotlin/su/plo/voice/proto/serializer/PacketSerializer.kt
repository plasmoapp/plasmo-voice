package su.plo.voice.proto.serializer

import com.google.common.io.ByteArrayDataInput
import com.google.common.io.ByteArrayDataOutput
import org.jetbrains.annotations.ApiStatus
import java.io.IOException

/**
 * The PacketSerializer interface defines the contract for objects that can be serialized
 * and deserialized to and from byte streams using a [ByteArrayDataInput] for deserialization
 * and a [ByteArrayDataOutput] for serialization. Implementing classes must provide methods
 * to serialize objects of type [T] into a byte stream and deserialize objects of type [T]
 * from a byte stream.
 *
 * @param T The type of object to be serialized and deserialized.
 */
interface PacketSerializer<T> {

    /**
     * Deserialize an object of type [T] from a byte stream using the provided [ByteArrayDataInput].
     * This method reconstructs the object from its serialized form.
     *
     * @param buffer The [ByteArrayDataInput] containing the serialized data.
     * @return The deserialized object of type [T].
     * @throws IOException If an I/O error occurs during deserialization.
     */
    @ApiStatus.Internal
    @Throws(IOException::class)
    fun deserialize(buffer: ByteArrayDataInput): T

    /**
     * Serialize an object of type [T] to a byte stream using the provided [ByteArrayDataOutput].
     * This method converts the object into its serialized form.
     *
     * @param obj The object of type [T] to be serialized.
     * @param buffer The [ByteArrayDataOutput] to which the serialized data should be written.
     * @throws IOException If an I/O error occurs during serialization.
     */
    @ApiStatus.Internal
    @Throws(IOException::class)
    fun serialize(obj: T, buffer: ByteArrayDataOutput)
}
