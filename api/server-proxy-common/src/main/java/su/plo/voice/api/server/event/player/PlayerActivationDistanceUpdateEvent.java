package su.plo.voice.api.server.event.player;

import lombok.Getter;
import lombok.NonNull;
import su.plo.voice.api.event.Event;
import su.plo.voice.api.server.audio.capture.ServerActivation;
import su.plo.voice.api.server.player.VoicePlayer;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This event is fired once a player updates the activation distance.
 */
public final class PlayerActivationDistanceUpdateEvent implements Event {

    @Getter
    private final VoicePlayer player;
    @Getter
    private final ServerActivation activation;
    @Getter
    private final int distance;
    @Getter
    private final int oldDistance;

    public PlayerActivationDistanceUpdateEvent(
            @NonNull VoicePlayer player,
            @NonNull ServerActivation activation,
            int distance,
            int oldDistance
    ) {
        this.player = checkNotNull(player, "player");
        this.activation = checkNotNull(activation, "activation");
        this.distance = distance;
        this.oldDistance = oldDistance;
    }
}
