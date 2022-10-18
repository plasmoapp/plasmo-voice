package su.plo.lib.mod.server.command;

import com.mojang.brigadier.CommandDispatcher;
import lombok.RequiredArgsConstructor;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.chat.MinecraftTextConverter;
import su.plo.lib.api.server.MinecraftServerLib;
import su.plo.lib.api.server.command.MinecraftCommandManager;

@RequiredArgsConstructor
public final class ModCommandManager extends MinecraftCommandManager {

    private final MinecraftServerLib minecraftServer;
    private final MinecraftTextConverter<Component> textConverter;

    public synchronized void registerCommands(@NotNull CommandDispatcher<CommandSourceStack> dispatcher) {
        commandByName.forEach((name, command) -> {
            ModCommand modCommand = new ModCommand(minecraftServer, textConverter, command);
            modCommand.register(dispatcher, name);
        });
        this.registered = true;
    }
}
