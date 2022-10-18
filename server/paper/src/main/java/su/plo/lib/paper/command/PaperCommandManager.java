package su.plo.lib.paper.command;

import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.server.MinecraftServerLib;
import su.plo.lib.api.server.command.MinecraftCommandManager;
import su.plo.lib.paper.chat.BaseComponentTextConverter;

@RequiredArgsConstructor
public final class PaperCommandManager extends MinecraftCommandManager {

    private final MinecraftServerLib minecraftServer;
    private final BaseComponentTextConverter textConverter;

    public synchronized void registerCommands(@NotNull JavaPlugin loader) {
        commandByName.forEach((name, command) -> {
            PaperCommand paperCommand = new PaperCommand(minecraftServer, textConverter, command, name);
            loader.getServer().getCommandMap().register("plasmovoice", paperCommand);

        });
        this.registered = true;
    }
}
