package su.plo.lib.mod.client.chat;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.client.MinecraftClientLib;
import su.plo.lib.mod.chat.ComponentTextConverter;
import su.plo.voice.client.chat.ClientLanguageSupplier;
import su.plo.voice.client.chat.ClientTextConverter;

public final class ClientComponentTextConverter extends ClientTextConverter<Component> {

    public ClientComponentTextConverter(@NotNull MinecraftClientLib minecraft,
                                        @NotNull ClientLanguageSupplier languageSupplier) {
        super(minecraft, languageSupplier, new ComponentTextConverter());
    }
}
