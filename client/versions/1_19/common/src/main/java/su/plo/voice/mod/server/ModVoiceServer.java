package su.plo.voice.mod.server;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.server.MinecraftServerLib;
import su.plo.lib.mod.server.ModServerLib;
import su.plo.voice.mod.server.connection.ModServerChannelHandler;
import su.plo.voice.server.BaseVoiceServer;

import java.io.File;

public abstract class ModVoiceServer<T extends ModServerChannelHandler> extends BaseVoiceServer {

    public static final ResourceLocation CHANNEL = new ResourceLocation(CHANNEL_STRING);

    protected final String modId = "plasmovoice";

    protected final ModServerLib minecraftServerLib = new ModServerLib();

    protected MinecraftServer server;
    protected T handler;

    protected void onInitialize(MinecraftServer server) {
        if (handler == null) {
            this.handler = createChannelHandler();
        }
        this.server = server;
        minecraftServerLib.setServer(server);
        minecraftServerLib.setPermissions(createPermissionSupplier());
        minecraftServerLib.onInitialize();
        super.onInitialize();
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
    public int getMinecraftServerPort() {
        return server != null ? server.getPort() : -1;
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
}
