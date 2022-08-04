package su.plo.voice.server;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.server.player.PlayerManager;
import su.plo.voice.server.player.ModPlayerManager;

import java.io.InputStream;

public abstract class ModVoiceServer extends BaseVoiceServer {

    public static final ResourceLocation CHANNEL = new ResourceLocation(CHANNEL_STRING);

    protected final String modId = "plasmovoice";

    protected MinecraftServer server;

    private PlayerManager players;

    @Override
    public @NotNull PlayerManager getPlayerManager() {
        return players;
    }

    @Override
    protected InputStream getResource(String name) {
        return getClass().getClassLoader().getResourceAsStream(name);
    }

    protected void onInitialize(MinecraftServer server) {
        this.server = server;

        this.players = new ModPlayerManager(this, server);
        eventBus.register(this, players);

        super.onInitialize();
    }

    protected void onShutdown(MinecraftServer server) {
        super.onShutdown();
    }
}
