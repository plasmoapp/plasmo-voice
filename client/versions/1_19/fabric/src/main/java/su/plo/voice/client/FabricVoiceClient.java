package su.plo.voice.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.client.connection.ServerConnection;
import su.plo.voice.client.connection.FabricClientChannelHandler;

import java.io.File;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

@Environment(EnvType.CLIENT)
public final class FabricVoiceClient extends VoiceClientMod implements ClientModInitializer {

    private final FabricClientChannelHandler handler = new FabricClientChannelHandler(this);

    @Override
    public void onInitializeClient() {
        super.onInitialize();

        // todo: должно ли это быть тут?
        ClientLifecycleEvents.CLIENT_STOPPING.register((minecraft) -> super.onShutdown());

        ClientPlayNetworking.registerGlobalReceiver(CHANNEL, handler);
    }

    @Override
    public @NotNull String getVersion() {
        ModContainer modContainer = FabricLoader.getInstance()
                .getModContainer(modId)
                .orElse(null);
        checkNotNull(modContainer, "modContainer cannot be null");
        return modContainer.getMetadata().getVersion().getFriendlyString();
    }

    @Override
    protected File configFolder() {
        return new File("config/" + modId);
    }

    @Override
    protected File modsFolder() {
        return new File("mods");
    }

    @Override
    protected File addonsFolder() {
        return new File(configFolder(), "addons");
    }

    @Override
    public Optional<ServerConnection> getServerConnection() {
        return handler.getConnection();
    }
}
