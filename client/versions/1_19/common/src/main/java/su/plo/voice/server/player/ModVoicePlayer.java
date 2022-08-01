package su.plo.voice.server.player;

import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.server.player.VoicePlayer;

import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ModVoicePlayer implements VoicePlayer {

    private final ServerPlayer player;

    public ModVoicePlayer(@NotNull ServerPlayer player) {
        this.player = checkNotNull(player, "player cannot be null");
    }

    @Override
    public @NotNull UUID getUUID() {
        return player.getUUID();
    }
}
