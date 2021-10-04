package su.plo.voice.client.event;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.BuiltInExceptionProvider;
import com.mojang.brigadier.exceptions.CommandExceptionType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import su.plo.voice.client.VoiceClientForge;
import su.plo.voice.client.commands.ForgeClientCommandSource;
import su.plo.voice.client.config.VoiceClientConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VoiceChatCommandEvent {
    private static final CommandDispatcher<ForgeClientCommandSource> DISPATCHER = new CommandDispatcher<>();

    /**
     * Creates a literal argument builder.
     *
     * @param name the literal name
     * @return the created argument builder
     */
    public static LiteralArgumentBuilder<ForgeClientCommandSource> literal(String name) {
        return LiteralArgumentBuilder.literal(name);
    }

    /**
     * Creates a required argument builder.
     *
     * @param name the name of the argument
     * @param type the type of the argument
     * @param <T>  the type of the parsed argument value
     * @return the created argument builder
     */
    public static <T> RequiredArgumentBuilder<ForgeClientCommandSource, T> argument(String name, ArgumentType<T> type) {
        return RequiredArgumentBuilder.argument(name, type);
    }

    public static void addCommands(CommandDispatcher<ForgeClientCommandSource> target, ForgeClientCommandSource source) {
        Map<CommandNode<ForgeClientCommandSource>, CommandNode<ForgeClientCommandSource>> originalToCopy = new HashMap<>();
        originalToCopy.put(DISPATCHER.getRoot(), target.getRoot());
        copyChildren(DISPATCHER.getRoot(), target.getRoot(), source, originalToCopy);
    }

    /**
     * Copies the child commands from origin to target, filtered by {@code child.canUse(source)}.
     * Mimics vanilla's CommandManager.makeTreeForSource.
     *
     * @param origin         the source command node
     * @param target         the target command node
     * @param source         the command source
     * @param originalToCopy a mutable map from original command nodes to their copies, used for redirects;
     *                       should contain a mapping from origin to target
     */
    private static void copyChildren(
            CommandNode<ForgeClientCommandSource> origin,
            CommandNode<ForgeClientCommandSource> target,
            ForgeClientCommandSource source,
            Map<CommandNode<ForgeClientCommandSource>, CommandNode<ForgeClientCommandSource>> originalToCopy
    ) {
        for (CommandNode<ForgeClientCommandSource> child : origin.getChildren()) {
            if (!child.canUse(source)) continue;

            ArgumentBuilder<ForgeClientCommandSource, ?> builder = child.createBuilder();

            // Reset the unnecessary non-completion stuff from the builder
            builder.requires(s -> true); // This is checked with the if check above.

            if (builder.getCommand() != null) {
                builder.executes(context -> 0);
            }

            // Set up redirects
            if (builder.getRedirect() != null) {
                builder.redirect(originalToCopy.get(builder.getRedirect()));
            }

            CommandNode<ForgeClientCommandSource> result = builder.build();
            originalToCopy.put(child, result);
            target.addChild(result);

            if (!child.getChildren().isEmpty()) {
                copyChildren(child, result, source, originalToCopy);
            }
        }
    }

    /**
     * Tests whether a command syntax exception with the type
     * should be ignored and the message sent to the server.
     *
     * @param type the exception type
     * @return true if ignored, false otherwise
     */
    private static boolean isIgnoredException(CommandExceptionType type) {
        BuiltInExceptionProvider builtins = CommandSyntaxException.BUILT_IN_EXCEPTIONS;

        // Only ignore unknown commands and node parse exceptions.
        // The argument-related dispatcher exceptions are not ignored because
        // they will only happen if the user enters a correct command.
        return type == builtins.dispatcherUnknownCommand() || type == builtins.dispatcherParseException();
    }

    public VoiceChatCommandEvent() {
        DISPATCHER.register(literal("vc")
                .then(literal("priority-distance")
                        .executes(ctx -> {
                            ctx.getSource().getPlayer().sendMessage(new TranslatableComponent("commands.plasmo_voice.priority_distance_set", VoiceClientForge.getServerConfig().getPriorityDistance()), new UUID(0, 0));
                            return 1;
                        })
                        .then(argument("distance", IntegerArgumentType.integer())
                                .executes(ctx -> {
                                    int distance = IntegerArgumentType.getInteger(ctx, "distance");
                                    if(distance <= VoiceClientForge.getServerConfig().getMaxDistance()) {
                                        ctx.getSource().getPlayer().sendMessage(new TranslatableComponent("commands.plasmo_voice.min_priority_distance", VoiceClientForge.getServerConfig().getMaxDistance()), new UUID(0, 0));
                                        return 1;
                                    }

                                    if(distance > VoiceClientForge.getServerConfig().getMaxPriorityDistance()) {
                                        ctx.getSource().getPlayer().sendMessage(new TranslatableComponent("commands.plasmo_voice.max_priority_distance", VoiceClientForge.getServerConfig().getMaxPriorityDistance()), new UUID(0, 0));
                                        return 1;
                                    }

                                    VoiceClientConfig.ServerConfig serverConfig;
                                    if (VoiceClientForge.getClientConfig().getServers()
                                            .containsKey(VoiceClientForge.getServerConfig().getIp())) {
                                        serverConfig = VoiceClientForge.getClientConfig().getServers()
                                                .get(VoiceClientForge.getServerConfig().getIp());
                                    } else {
                                        serverConfig = new VoiceClientConfig.ServerConfig();
                                        serverConfig.distance.setDefault((int) VoiceClientForge.getServerConfig().getDefaultDistance());
                                        VoiceClientForge.getClientConfig().getServers().put(VoiceClientForge.getServerConfig().getIp(), serverConfig);
                                    }

                                    serverConfig.priorityDistance.set(distance);
                                    VoiceClientForge.getServerConfig().setPriorityDistance((short) distance);
                                    VoiceClientForge.getClientConfig().save();
                                    ctx.getSource().getPlayer().sendMessage(new TranslatableComponent("commands.plasmo_voice.priority_distance_set", distance), new UUID(0, 0));
                                    return 1;
                                }))));
    }

    @SubscribeEvent
    public void onClientChatEvent(ClientChatEvent e) {
        if(executeCommand(e.getMessage())) {
            Minecraft.getInstance().gui.getChat().addRecentChat(e.getMessage());
            e.setCanceled(true);
        }
    }

    private boolean executeCommand(String message) {
        if (message.isEmpty()) {
            return false; // Nothing to process
        }

        if (message.charAt(0) != '/') {
            return false;
        }

        Minecraft client = Minecraft.getInstance();


        ForgeClientCommandSource commandSource = (ForgeClientCommandSource) client.getConnection().getSuggestionsProvider();
        client.getProfiler().push(message);

        try {
            // TODO: Check for server commands before executing.
            //   This requires parsing the command, checking if they match a server command
            //   and then executing the command with the parse results.
            DISPATCHER.execute(message.substring(1), commandSource);
            return true;
        } catch (CommandSyntaxException e) {
            if (isIgnoredException(e.getType())) {
                return false;
            }

            commandSource.sendError(getErrorMessage(e));
            return true;
        } catch (RuntimeException e) {
            commandSource.sendError(new TextComponent(e.getMessage()));
            return true;
        } finally {
            client.getProfiler().pop();
        }
    }

    private static Component getErrorMessage(CommandSyntaxException e) {
        Component message = ComponentUtils.fromMessage(e.getRawMessage());
        String context = e.getContext();

        return context != null ? new TranslatableComponent("command.context.parse_error", message, context) : message;
    }
}
