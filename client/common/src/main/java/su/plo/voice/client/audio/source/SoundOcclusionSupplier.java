package su.plo.voice.client.audio.source;

public interface SoundOcclusionSupplier {

    double getOccludedPercent(float[] position);
}
