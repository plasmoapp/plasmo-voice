package su.plo.lib.mod.server.chat;

import gg.essential.universal.wrappers.message.UTextComponent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.chat.MinecraftTextComponent;
import su.plo.lib.api.server.chat.ServerTextConverter;
import su.plo.lib.mod.chat.ComponentTextConverter;
import su.plo.voice.api.server.config.ServerLanguages;

import java.util.function.Supplier;

public final class ServerComponentTextConverter extends ServerTextConverter<UTextComponent> {

    private final ComponentTextConverter textConverter;

    public ServerComponentTextConverter(@NotNull ComponentTextConverter textConverter,
                                        @NotNull Supplier<ServerLanguages> languagesSupplier) {
        super(languagesSupplier);
        this.textConverter = textConverter;
    }

    @Override
    public UTextComponent convert(@NotNull MinecraftTextComponent text) {
        return textConverter.convert(text);
    }
}
