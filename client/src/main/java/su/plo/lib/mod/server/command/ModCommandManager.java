package su.plo.lib.mod.server.command;

import com.mojang.brigadier.CommandDispatcher;
import gg.essential.universal.wrappers.message.UTextComponent;
import lombok.RequiredArgsConstructor;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.server.MinecraftServerLib;
import su.plo.lib.api.server.chat.ServerTextConverter;
import su.plo.lib.api.server.command.MinecraftCommand;
import su.plo.lib.api.server.command.MinecraftCommandManager;

@RequiredArgsConstructor
public final class ModCommandManager extends MinecraftCommandManager<MinecraftCommand> {

    private final MinecraftServerLib minecraftServer;
    private final ServerTextConverter<UTextComponent> textConverter;

    public synchronized void registerCommands(@NotNull CommandDispatcher<CommandSourceStack> dispatcher) {
        commandByName.forEach((name, command) -> {
            ModCommand modCommand = new ModCommand(minecraftServer, textConverter, command);
            modCommand.register(dispatcher, name);
        });
        this.registered = true;
    }
}
