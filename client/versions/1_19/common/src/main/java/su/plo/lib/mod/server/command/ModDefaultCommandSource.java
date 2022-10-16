package su.plo.lib.mod.server.command;

import lombok.RequiredArgsConstructor;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.chat.TextComponent;
import su.plo.lib.api.chat.TextConverter;
import su.plo.lib.api.server.command.MinecraftCommandSource;
import su.plo.lib.api.server.permission.PermissionTristate;

@RequiredArgsConstructor
public final class ModDefaultCommandSource implements MinecraftCommandSource {

    private final CommandSourceStack source;
    private final TextConverter<Component> textConverter;

    @Override
    public void sendMessage(@NotNull TextComponent text) {
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
        return false;
    }

    @Override
    public @NotNull PermissionTristate getPermission(@NotNull String permission) {
        return PermissionTristate.FALSE;
    }
}
