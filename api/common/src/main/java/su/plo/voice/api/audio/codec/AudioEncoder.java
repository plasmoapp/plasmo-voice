package su.plo.voice.api.audio.codec;

// todo: doc
public interface AudioEncoder {

    byte[] encode(short[] samples) throws CodecException;

    void open() throws CodecException;

    void reset();

    void close();

    boolean isOpen();
}
