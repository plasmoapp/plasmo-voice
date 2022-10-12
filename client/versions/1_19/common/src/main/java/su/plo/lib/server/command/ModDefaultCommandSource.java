package su.plo.lib.server.command;

import lombok.RequiredArgsConstructor;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.chat.TextComponent;
import su.plo.lib.server.permission.PermissionTristate;

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

    @Override
    public @NotNull PermissionTristate getPermission(@NotNull String permission) {
        return PermissionTristate.FALSE;
    }
}
