package su.plo.lib.mod.server.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import lombok.RequiredArgsConstructor;
import net.minecraft.commands.CommandSourceStack;
import su.plo.lib.api.chat.MinecraftTextComponent;
import su.plo.lib.api.server.command.MinecraftCommand;
import su.plo.lib.api.server.command.MinecraftCommandSource;
import su.plo.voice.BaseVoice;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

@RequiredArgsConstructor
public final class ModCommand implements Command<CommandSourceStack>, Predicate<CommandSourceStack>, SuggestionProvider<CommandSourceStack> {

    private final ModCommandManager commandManager;
    private final MinecraftCommand command;

    public LiteralCommandNode<CommandSourceStack> register(CommandDispatcher<CommandSourceStack> dispatcher, String label) {
        RootCommandNode<CommandSourceStack> root = dispatcher.getRoot();
        LiteralCommandNode<CommandSourceStack> literal = LiteralArgumentBuilder.<CommandSourceStack>literal(label).requires(this).executes(this).build();
        ArgumentCommandNode<CommandSourceStack, String> defaultArgs = RequiredArgumentBuilder.<CommandSourceStack, String>argument("args", StringArgumentType.greedyString()).suggests(this).executes(this).build();
        literal.addChild(defaultArgs);

        root.addChild(literal);
        return literal;
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        MinecraftCommandSource source = commandManager.getCommandSource(context.getSource());

        int spaceIndex = context.getInput().indexOf(' ');
        String[] args;
        if (spaceIndex >= 0) {
            args = context.getInput().substring(spaceIndex + 1).split(" ", -1);
        } else {
            args = new String[0];
        }

        if (!command.hasPermission(source, args)) {
            source.sendMessage(MinecraftTextComponent.translatable("pv.error.no_permissions"));
            return 1;
        }

        try {
            command.execute(source, args);
        } catch (Exception e) {
            BaseVoice.LOGGER.error("Error while executing command", e);
            throw e;
        }
        return 1;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        int spaceIndex = context.getInput().indexOf(' ');
        String[] args = context.getInput().substring(spaceIndex + 1).split(" ", -1);

        List<String> results = command.suggest(commandManager.getCommandSource(context.getSource()), args);

        // Defaults to sub nodes, but we have just one giant args node, so offset accordingly
        builder = builder.createOffset(builder.getInput().lastIndexOf(' ') + 1);

        for (String s : results) {
            builder.suggest(s);
        }

        return builder.buildFuture();
    }

    @Override
    public boolean test(CommandSourceStack source) {
        return command.hasPermission(commandManager.getCommandSource(source), null);
    }
}
