package su.plo.voice.api.server.event.audio.source;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.event.EventCancellableBase;
import su.plo.voice.api.event.EventPriority;
import su.plo.voice.api.event.EventSubscribe;
import su.plo.voice.api.server.audio.capture.ServerActivation;
import su.plo.voice.api.server.event.audio.capture.PlayerServerActivationEndEvent;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.proto.packets.tcp.serverbound.PlayerAudioEndPacket;

/**
 * This event is fired when the {@link PlayerAudioEndPacket} is received.
 * <br/>
 * This event is cancelled if activation was handled successfully by
 * {@link ServerActivation#onPlayerActivationEnd(ServerActivation.PlayerActivationEndListener)}.
 * <br/>
 * By default, {@link EventSubscribe} ignores cancelled events,
 * so if you want to listen for this event, make sure to set {@link EventSubscribe#ignoreCancelled()} to {@code false}
 * or {@link EventSubscribe#priority()} to {@link EventPriority#LOWEST} to handle it before it handled by the internal listener.
 * <br/>
 * Or use activation-related events, they're not cancelable.
 *
 * @see PlayerServerActivationEndEvent
 */
public final class PlayerSpeakEndEvent extends EventCancellableBase {

    @Getter
    private final VoicePlayer player;
    @Getter
    private final PlayerAudioEndPacket packet;

    @Getter
    @Setter
    private ServerActivation.Result result = ServerActivation.Result.IGNORED;

    public PlayerSpeakEndEvent(@NotNull VoicePlayer player,
                               @NotNull PlayerAudioEndPacket packet) {
        this.player = player;
        this.packet = packet;
    }
}
