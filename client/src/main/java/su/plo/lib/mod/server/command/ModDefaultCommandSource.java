package su.plo.lib.mod.server.command;

import gg.essential.universal.wrappers.message.UTextComponent;
import lombok.RequiredArgsConstructor;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.chat.MinecraftTextComponent;
import su.plo.lib.api.server.chat.ServerTextConverter;
import su.plo.lib.api.server.command.MinecraftCommandSource;
import su.plo.lib.api.server.permission.PermissionTristate;

@RequiredArgsConstructor
public final class ModDefaultCommandSource implements MinecraftCommandSource {

    private final CommandSourceStack source;
    private final ServerTextConverter<UTextComponent> textConverter;

    @Override
    public void sendMessage(@NotNull String text) {
        source.sendSystemMessage(Component.literal(text));
    }

    @Override
    public void sendMessage(@NotNull MinecraftTextComponent text) {
        source.sendSystemMessage(textConverter.convert(this, text));
    }

    @Override
    public void sendActionBar(@NotNull String text) {
        source.sendSystemMessage(Component.literal(text));
    }

    @Override
    public void sendActionBar(@NotNull MinecraftTextComponent text) {
        source.sendSystemMessage(textConverter.convert(this, text));
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
