package su.plo.lib.velocity.command;

import com.velocitypowered.api.command.CommandSource;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.chat.MinecraftTextComponent;
import su.plo.lib.api.server.command.MinecraftCommandSource;
import su.plo.lib.api.server.permission.PermissionTristate;
import su.plo.lib.velocity.chat.ComponentTextConverter;

@RequiredArgsConstructor
public final class VelocityDefaultCommandSource implements MinecraftCommandSource {

    private final CommandSource source;
    private final ComponentTextConverter textConverter;

    @Override
    public void sendMessage(@NotNull MinecraftTextComponent text) {
        source.sendMessage(textConverter.convert(this, text));
    }

    @Override
    public void sendMessage(@NotNull String text) {
        source.sendMessage(Component.text(text));
    }

    @Override
    public void sendActionBar(@NotNull String text) {
        source.sendActionBar(Component.text(text));
    }

    @Override
    public void sendActionBar(@NotNull MinecraftTextComponent text) {
        source.sendActionBar(textConverter.convert(this, text));
    }

    @Override
    public @NotNull String getLanguage() {
        return "en_us";
    }

    @Override
    public boolean hasPermission(@NotNull String permission) {
        return source.hasPermission(permission);
    }

    @Override
    public @NotNull PermissionTristate getPermission(@NotNull String permission) {
        switch (source.getPermissionValue(permission)) {
            case TRUE:
                return PermissionTristate.TRUE;
            case FALSE:
                return PermissionTristate.FALSE;
            default:
                return PermissionTristate.UNDEFINED;
        }
    }
}
