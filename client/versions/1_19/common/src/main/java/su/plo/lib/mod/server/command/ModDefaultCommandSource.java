package su.plo.lib.mod.server.command;

import lombok.RequiredArgsConstructor;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.chat.MinecraftTextComponent;
import su.plo.lib.api.chat.MinecraftTextConverter;
import su.plo.lib.api.server.command.MinecraftCommandSource;
import su.plo.lib.api.server.permission.PermissionTristate;

@RequiredArgsConstructor
public final class ModDefaultCommandSource implements MinecraftCommandSource {

    private final CommandSourceStack source;
    private final MinecraftTextConverter<Component> textConverter;

    @Override
    public void sendMessage(@NotNull MinecraftTextComponent text) {
        source.sendSystemMessage(textConverter.convert(text));
    }

    @Override
    public void sendMessage(@NotNull String text) {
        source.sendSystemMessage(Component.literal(text));
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
