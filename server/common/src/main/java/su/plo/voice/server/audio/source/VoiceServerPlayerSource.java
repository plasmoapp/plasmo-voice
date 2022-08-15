package su.plo.voice.server.audio.source;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.server.audio.source.ServerPlayerSource;
import su.plo.voice.api.server.connection.UdpServerConnectionManager;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.api.server.pos.ServerPos3d;
import su.plo.voice.proto.data.source.PlayerSourceInfo;
import su.plo.voice.proto.data.source.SourceInfo;

import java.util.UUID;

public class VoiceServerPlayerSource extends BaseServerSource implements ServerPlayerSource {

    private final VoicePlayer player;
    private final ServerPos3d playerPosition = new ServerPos3d();

    public VoiceServerPlayerSource(UdpServerConnectionManager udpConnections, @NotNull UUID id, @NotNull String codec, @NotNull VoicePlayer player) {
        super(udpConnections, id, codec);
        this.player = player;
        addFilter(this::filterVanish);
    }

    @Override
    public @NotNull VoicePlayer getPlayer() {
        return player;
    }

    @Override
    public @NotNull ServerPos3d getPosition() {
        return player.getPosition(playerPosition);
    }

    private boolean filterVanish(@NotNull VoicePlayer player) {
        return
//                !player.equals(this.player) &&
                player.canSee(this.player);
    }

    @Override
    public @NotNull SourceInfo getInfo() {
        return new PlayerSourceInfo(id, true, player.getInfo());
    }
}
