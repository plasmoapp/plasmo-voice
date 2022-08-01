package su.plo.voice.server;

import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.server.player.PlayerManager;
import su.plo.voice.server.player.ModPlayerManager;

public abstract class ModVoiceServer extends VoiceServerBase {

    protected final String modId = "plasmovoice";

    protected MinecraftServer server;

    private PlayerManager players;

    @Override
    public @NotNull PlayerManager getPlayerManager() {
        return players;
    }

    protected void onInitialize(MinecraftServer server) {
        this.server = server;
        super.onInitialize();

        this.players = new ModPlayerManager(server);
        eventBus.register(this, players);
    }

    protected void onShutdown(MinecraftServer server) {
        super.onShutdown();
    }
}
