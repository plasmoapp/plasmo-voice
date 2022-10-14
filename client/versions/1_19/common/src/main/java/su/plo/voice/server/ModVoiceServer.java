package su.plo.voice.server;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.server.MinecraftServerLib;
import su.plo.lib.server.ModServerLib;

public abstract class ModVoiceServer extends BaseVoiceServer {

    public static final ResourceLocation CHANNEL = new ResourceLocation(CHANNEL_STRING);

    protected final String modId = "plasmovoice";

    protected final ModServerLib minecraftServerLib = new ModServerLib();

    protected MinecraftServer server;

    protected void onInitialize(MinecraftServer server) {
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
    }

    protected void onCommandRegister(@NotNull CommandDispatcher<CommandSourceStack> dispatcher) {
        registerDefaultCommandsAndPermissions();
        minecraftServerLib.getCommandManager().registerCommands(dispatcher);
    }

    @Override
    public @NotNull MinecraftServerLib getMinecraftServer() {
        return minecraftServerLib;
    }
}
