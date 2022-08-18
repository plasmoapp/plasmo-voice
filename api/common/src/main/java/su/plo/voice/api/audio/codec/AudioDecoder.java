package su.plo.voice.api.audio.codec;

// todo: doc
public interface AudioDecoder {

    short[] decode(byte[] encoded) throws CodecException;

    void open() throws CodecException;

    void reset();

    void close();

    boolean isOpen();
}
