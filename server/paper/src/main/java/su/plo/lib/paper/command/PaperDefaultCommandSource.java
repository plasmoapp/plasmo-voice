package su.plo.lib.paper.command;

import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.chat.MinecraftTextComponent;
import su.plo.lib.api.server.command.MinecraftCommandSource;
import su.plo.lib.api.server.permission.PermissionTristate;
import su.plo.lib.paper.chat.BaseComponentTextConverter;

@RequiredArgsConstructor
public final class PaperDefaultCommandSource implements MinecraftCommandSource {

    private final CommandSender source;
    private final BaseComponentTextConverter textConverter;

    @Override
    public void sendMessage(@NotNull MinecraftTextComponent text) {
        source.sendMessage(textConverter.convert(this, text));
    }

    @Override
    public void sendMessage(@NotNull String text) {
        source.sendMessage(text);
    }

    @Override
    public void sendActionBar(@NotNull String text) {
        source.sendMessage(text);
    }

    @Override
    public void sendActionBar(@NotNull MinecraftTextComponent text) {
        source.sendMessage(textConverter.convert(this, text));
    }

    @Override
    public @NotNull String getLanguage() {
        return "en_us";
    }

    @Override
    public boolean hasPermission(@NotNull String permission) {
        return true;
    }

    @Override
    public @NotNull PermissionTristate getPermission(@NotNull String permission) {
        return PermissionTristate.FALSE;
    }
}
