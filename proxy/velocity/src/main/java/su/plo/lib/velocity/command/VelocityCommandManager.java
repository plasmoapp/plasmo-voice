package su.plo.lib.velocity.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.proxy.MinecraftProxyLib;
import su.plo.lib.api.proxy.command.MinecraftProxyCommand;
import su.plo.lib.api.proxy.event.command.MinecraftCommandExecuteEvent;
import su.plo.lib.api.server.command.MinecraftCommandManager;
import su.plo.lib.api.server.command.MinecraftCommandSource;
import su.plo.lib.velocity.chat.ComponentTextConverter;

@RequiredArgsConstructor
public final class VelocityCommandManager extends MinecraftCommandManager<MinecraftProxyCommand> {

    private final MinecraftProxyLib minecraftProxy;
    private final ComponentTextConverter textConverter;

    @Subscribe
    public void onCommandExecute(@NotNull CommandExecuteEvent event) {
        String command = event.getCommand();
        String commandAlias = command.split(" ")[0];

        MinecraftCommandSource commandSource = getCommandSource(event.getCommandSource());
        minecraftProxy.getEventBus().call(new MinecraftCommandExecuteEvent(commandSource, command));

        MinecraftProxyCommand proxyCommand = commandByName.get(commandAlias);
        if (proxyCommand == null) return;

        int spaceIndex = command.indexOf(' ');
        String[] args;
        if (spaceIndex >= 0) {
            args = command.substring(spaceIndex + 1).split(" ", -1);
        } else {
            args = new String[0];
        }

        if (proxyCommand.passToBackendServer(commandSource, args)) {
            event.setResult(CommandExecuteEvent.CommandResult.forwardToServer());
        }
    }

    public synchronized void registerCommands(@NotNull ProxyServer proxyServer) {
        commandByName.forEach((name, command) -> {
            // todo: group commands and use aliases?
            VelocityCommand velocityCommand = new VelocityCommand(this, minecraftProxy, textConverter, command);
            proxyServer.getCommandManager().register(name, velocityCommand);
        });
        this.registered = true;
    }

    public MinecraftCommandSource getCommandSource(@NotNull CommandSource source) {
        if (source instanceof Player) {
            return minecraftProxy.getPlayerByInstance(source);
        }

        return new VelocityDefaultCommandSource(source, textConverter);
    }
}
