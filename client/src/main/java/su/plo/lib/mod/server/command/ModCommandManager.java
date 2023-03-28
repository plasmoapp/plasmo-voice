package su.plo.lib.mod.server.command;

import com.mojang.brigadier.CommandDispatcher;
import lombok.RequiredArgsConstructor;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.server.MinecraftServerLib;
import su.plo.lib.api.server.chat.ServerTextConverter;
import su.plo.lib.api.server.command.MinecraftCommand;
import su.plo.lib.api.server.command.MinecraftCommandManager;
import su.plo.lib.api.server.command.MinecraftCommandSource;

@RequiredArgsConstructor
public final class ModCommandManager extends MinecraftCommandManager<MinecraftCommand> {

    private final MinecraftServerLib minecraftServer;
    private final ServerTextConverter<Component> textConverter;

    public synchronized void registerCommands(@NotNull CommandDispatcher<CommandSourceStack> dispatcher) {
        commandByName.forEach((name, command) -> {
            ModCommand modCommand = new ModCommand(this, command);
            modCommand.register(dispatcher, name);
        });
        this.registered = true;
    }

    @Override
    public MinecraftCommandSource getCommandSource(@NotNull Object source) {
        if (!(source instanceof CommandSourceStack))
            throw new IllegalArgumentException("source is not " + CommandSourceStack.class);

        Entity entity = ((CommandSourceStack) source).getEntity();
        if (entity instanceof Player) {
            return minecraftServer.getPlayerByInstance(entity);
        }

        return new ModDefaultCommandSource(((CommandSourceStack) source), textConverter);
    }
}
