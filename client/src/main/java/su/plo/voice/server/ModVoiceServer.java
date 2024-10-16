package su.plo.voice.server;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;
import su.plo.slib.api.server.McServerLib;
import su.plo.slib.mod.ModServerLib;
import su.plo.slib.mod.event.ModServerEvents;
import su.plo.slib.mod.event.server.ServerStartedEvent;
import su.plo.slib.mod.event.server.ServerStoppingEvent;
import su.plo.voice.util.version.ModrinthLoader;

import java.io.File;

public final class ModVoiceServer extends BaseVoiceServer {

    // static instance is used for access from mixins
    public static ModVoiceServer INSTANCE;

    public static final ResourceLocation CHANNEL = ResourceLocation.tryParse(CHANNEL_STRING);
    public static final ResourceLocation FLAG_CHANNEL = ResourceLocation.tryParse(FLAG_CHANNEL_STRING);
    public static final ResourceLocation SERVICE_CHANNEL = ResourceLocation.tryParse(SERVICE_CHANNEL_STRING);

    private final String modId = "plasmovoice";

    public ModVoiceServer(@NotNull ModrinthLoader loader) {
        super(loader);
        INSTANCE = this;
    }

    @Override
    public @NotNull File getConfigFolder() {
        return new File("config/" + modId + "/server");
    }

    @Override
    public @NotNull McServerLib getMinecraftServer() {
        return ModServerLib.INSTANCE;
    }

    @Override
    public void onInitialize() {
        ModServerEvents.initialize();

        ServerStartedEvent.INSTANCE.registerListener(this::onMinecraftServerInitialize);
        ServerStoppingEvent.INSTANCE.registerListener(this::onMinecraftServerShutdown);
    }

    private void onMinecraftServerInitialize(@NotNull MinecraftServer server) {
        super.onInitialize();
    }

    private void onMinecraftServerShutdown(MinecraftServer server) {
        super.onShutdown();
    }
}
