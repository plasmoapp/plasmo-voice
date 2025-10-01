package su.plo.voice.api.server.event.audio.source;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.event.EventCancellableBase;
import su.plo.voice.api.event.EventPriority;
import su.plo.voice.api.event.EventSubscribe;
import su.plo.voice.api.server.audio.capture.ServerActivation;
import su.plo.voice.api.server.event.audio.capture.PlayerServerActivationEvent;
import su.plo.voice.api.server.event.audio.capture.PlayerServerActivationStartEvent;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.proto.packets.udp.serverbound.PlayerAudioPacket;

/**
 * This event is fired when the {@link PlayerAudioPacket} is received.
 * <br/>
 * This event is cancelled if activation was handled successfully by
 * {@link ServerActivation#onPlayerActivation(ServerActivation.PlayerActivationListener)}.
 * <br/>
 * By default, {@link EventSubscribe} ignores cancelled events,
 * so if you want to listen for this event, make sure to set {@link EventSubscribe#ignoreCancelled()} to {@code false}
 * or {@link EventSubscribe#priority()} to {@link EventPriority#LOWEST} to handle it before it handled by the internal listener.
 * <br/>
 * Or use activation-related events, they're not cancelable.
 *
 * @see PlayerServerActivationEvent
 * @see PlayerServerActivationStartEvent
 */
public final class PlayerSpeakEvent extends EventCancellableBase {

    @Getter
    private final VoicePlayer player;
    @Getter
    private final PlayerAudioPacket packet;

    @Getter
    @Setter
    private ServerActivation.Result result = ServerActivation.Result.IGNORED;

    public PlayerSpeakEvent(@NotNull VoicePlayer player,
                            @NotNull PlayerAudioPacket packet) {
        this.player = player;
        this.packet = packet;
    }
}
