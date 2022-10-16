package su.plo.lib.mod.server.command;

import com.mojang.brigadier.CommandDispatcher;
import lombok.RequiredArgsConstructor;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.chat.TextConverter;
import su.plo.lib.api.server.MinecraftServerLib;
import su.plo.lib.api.server.command.MinecraftCommand;
import su.plo.lib.api.server.command.MinecraftCommandManager;

@RequiredArgsConstructor
public final class ModCommandManager extends MinecraftCommandManager {

    private final MinecraftServerLib minecraftServer;
    private final TextConverter<Component> textConverter;

    private boolean registered;

    @Override
    public synchronized void register(@NotNull String name, @NotNull MinecraftCommand command, String... aliases) {
        if (registered) throw new IllegalStateException("register after commands registration is not supported");

        super.register(name, command, aliases);
    }

    @Override
    public synchronized boolean unregister(@NotNull String name) {
        throw new IllegalStateException("unregister is not supported");
    }

    @Override
    public synchronized void clear() {
        super.clear();
        this.registered = false;
    }

    public synchronized void registerCommands(@NotNull CommandDispatcher<CommandSourceStack> dispatcher) {
        commandByName.forEach((name, command) -> {
            ModCommand modCommand = new ModCommand(minecraftServer, textConverter, command);
            modCommand.register(dispatcher, name);
        });
        this.registered = true;
    }
}
