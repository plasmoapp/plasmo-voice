package su.plo.voice.mod.server;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.server.MinecraftServerLib;
import su.plo.lib.mod.server.ModServerLib;
import su.plo.voice.mod.server.connection.ModServerChannelHandler;
import su.plo.voice.mod.server.connection.ModServerServiceChannelHandler;
import su.plo.voice.server.BaseVoiceServer;

import java.io.File;

public abstract class ModVoiceServer<T extends ModServerChannelHandler, S extends ModServerServiceChannelHandler> extends BaseVoiceServer {

    public static final ResourceLocation CHANNEL = new ResourceLocation(CHANNEL_STRING);
    public static final ResourceLocation SERVICE_CHANNEL = new ResourceLocation(SERVICE_CHANNEL_STRING);

    protected final String modId = "plasmovoice";

    protected final ModServerLib minecraftServerLib = new ModServerLib();

    protected MinecraftServer server;
    protected T handler;
    protected S serviceHandler;

    protected void onInitialize(MinecraftServer server) {
        this.server = server;
        minecraftServerLib.setServer(server);
        minecraftServerLib.setPermissions(createPermissionSupplier());
        minecraftServerLib.onInitialize();
        super.onInitialize();
        if (handler == null) {
            this.handler = createChannelHandler();
        }

        if (serviceHandler == null) {
            this.serviceHandler = createServiceChannelHandler();
        }
    }

    protected void onShutdown(MinecraftServer server) {
        super.onShutdown();
        this.server = null;
        minecraftServerLib.onShutdown();
        handler.clear();
    }

    protected void onCommandRegister(@NotNull CommandDispatcher<CommandSourceStack> dispatcher) {
        registerDefaultCommandsAndPermissions();
        minecraftServerLib.getCommandManager().registerCommands(dispatcher);
    }

    @Override
    public @NotNull File getConfigFolder() {
        return new File("config/" + modId);
    }

    @Override
    protected File modsFolder() {
        return new File("mods");
    }

    @Override
    public @NotNull MinecraftServerLib getMinecraftServer() {
        return minecraftServerLib;
    }

    protected abstract T createChannelHandler();

    protected abstract S createServiceChannelHandler();
}
