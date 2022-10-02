package su.plo.lib.server.command;

import lombok.RequiredArgsConstructor;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.chat.TextComponent;

@RequiredArgsConstructor
public final class ModDefaultCommandSource implements MinecraftCommandSource {

    private final CommandSourceStack source;

    @Override
    public void sendMessage(@NotNull TextComponent text) {

    }

    @Override
    public boolean hasPermission(@NotNull String permission) {
        return false;
    }
}
