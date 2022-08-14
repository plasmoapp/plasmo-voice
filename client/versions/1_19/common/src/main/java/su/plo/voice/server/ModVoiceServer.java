package su.plo.voice.server;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.server.entity.EntityManager;
import su.plo.voice.api.server.player.PlayerManager;
import su.plo.voice.api.server.pos.WorldManager;
import su.plo.voice.server.entity.ModEntityManager;
import su.plo.voice.server.player.ModPlayerManager;
import su.plo.voice.server.pos.ModWorldManager;

import java.io.InputStream;

public abstract class ModVoiceServer extends BaseVoiceServer {

    public static final ResourceLocation CHANNEL = new ResourceLocation(CHANNEL_STRING);

    protected final String modId = "plasmovoice";

    protected MinecraftServer server;

    private PlayerManager players;
    private EntityManager entities;
    private WorldManager worlds;

    @Override
    public @NotNull PlayerManager getPlayerManager() {
        return players;
    }

    @Override
    public @NotNull EntityManager getEntityManager() {
        return entities;
    }

    @Override
    public @NotNull WorldManager getWorldManager() {
        return worlds;
    }

    @Override
    protected InputStream getResource(String name) {
        return getClass().getClassLoader().getResourceAsStream(name);
    }

    protected void onInitialize(MinecraftServer server) {
        this.server = server;

        this.players = new ModPlayerManager(this, server);
        this.entities = new ModEntityManager(server);
        this.worlds = new ModWorldManager();
        eventBus.register(this, players);

        super.onInitialize();
    }

    protected void onShutdown(MinecraftServer server) {
        super.onShutdown();
    }
}
