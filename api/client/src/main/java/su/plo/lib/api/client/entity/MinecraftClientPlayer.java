package su.plo.lib.api.client.entity;

import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.chat.MinecraftTextComponent;
import su.plo.lib.api.entity.MinecraftPlayerEntity;

public interface MinecraftClientPlayer extends MinecraftPlayerEntity {

    void sendChatMessage(@NotNull MinecraftTextComponent text);

    void sendActionbarMessage(@NotNull MinecraftTextComponent text);
}
