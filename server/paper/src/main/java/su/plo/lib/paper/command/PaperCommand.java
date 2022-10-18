package su.plo.lib.paper.command;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.lib.api.chat.MinecraftTextComponent;
import su.plo.lib.api.chat.MinecraftTextStyle;
import su.plo.lib.api.server.MinecraftServerLib;
import su.plo.lib.api.server.command.MinecraftCommand;
import su.plo.lib.api.server.command.MinecraftCommandSource;
import su.plo.lib.paper.chat.BaseComponentTextConverter;

import java.util.List;

public final class PaperCommand extends Command {

    private final MinecraftServerLib minecraftServer;
    private final BaseComponentTextConverter textConverter;
    private final MinecraftCommand command;

    public PaperCommand(@NotNull MinecraftServerLib minecraftServer,
                        @NotNull BaseComponentTextConverter textConverter,
                        @NotNull MinecraftCommand command,
                        @NotNull String name) {
        super(name);

        this.minecraftServer = minecraftServer;
        this.textConverter = textConverter;
        this.command = command;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        MinecraftCommandSource source = getCommandSource(sender);

        if (!command.hasPermission(source, args)) {
            source.sendMessage( // todo: use ServerLanguage?
                    MinecraftTextComponent.literal("I'm sorry, but you do not have permission to perform this command. Please contact the server administrators if you believe that this is in error.")
                            .withStyle(MinecraftTextStyle.RED)
            );
            return true;
        }

        command.execute(source, args);
        return true;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender,
                                             @NotNull String alias,
                                             @NotNull String[] args,
                                             @Nullable Location location) throws IllegalArgumentException {
        return command.suggest(getCommandSource(sender), args);
    }

    @Override
    public boolean testPermissionSilent(@NotNull CommandSender target) {
        return command.hasPermission(getCommandSource(target), null);
    }

    private MinecraftCommandSource getCommandSource(CommandSender source) {
        if (source instanceof Player) {
            return minecraftServer.getPlayerByInstance(source);
        }

        return new PaperDefaultCommandSource(source, textConverter);
    }
}
