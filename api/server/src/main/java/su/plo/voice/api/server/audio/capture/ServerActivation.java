package su.plo.voice.api.server.audio.capture;

import su.plo.voice.proto.data.capture.Activation;

import java.util.List;

public interface ServerActivation extends Activation {

    /**
     * Sets the activation's available distances
     */
    void setDistances(List<Integer> distances);

    /**
     * Sets the activation's transitivity
     */
    void setTransitive(boolean transitive);
}
