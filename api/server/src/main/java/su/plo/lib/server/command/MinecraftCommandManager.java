package su.plo.lib.server.command;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public abstract class MinecraftCommandManager {

    protected final Map<String, MinecraftCommand> commandByName = Maps.newHashMap();

    public synchronized void register(@NotNull String name, @NotNull MinecraftCommand command, String... aliases) {
        if (commandByName.containsKey(name)) {
            throw new IllegalArgumentException("Command with name '" + name + "' already exist");
        }

        for (String alias : aliases) {
            if (commandByName.containsKey(alias)) {
                throw new IllegalArgumentException("Command with name '" + alias + "' already exist");
            }
        }

        commandByName.put(name, command);
        for (String alias : aliases) {
            commandByName.put(alias, command);
        }
    }

    public synchronized boolean unregister(@NotNull String name) {
        return commandByName.remove(name) != null;
    }

    public synchronized Map<String, MinecraftCommand> getRegisteredCommands() {
        return ImmutableMap.copyOf(commandByName);
    }

    public synchronized void clear() {
        commandByName.clear();
    }
}
