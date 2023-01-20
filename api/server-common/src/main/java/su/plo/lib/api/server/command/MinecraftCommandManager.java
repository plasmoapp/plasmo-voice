package su.plo.lib.api.server.command;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public abstract class MinecraftCommandManager<T extends MinecraftCommand> {

    protected final Map<String, T> commandByName = Maps.newHashMap();
    protected boolean registered;

    public synchronized void register(@NotNull String name, @NotNull T command, String... aliases) {
        if (registered) throw new IllegalStateException("register after commands registration is not supported");

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

    public synchronized Map<String, MinecraftCommand> getRegisteredCommands() {
        return ImmutableMap.copyOf(commandByName);
    }

    public synchronized void clear() {
        commandByName.clear();
        this.registered = false;
    }
}
