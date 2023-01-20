package su.plo.lib.api.client.connection;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import su.plo.voice.proto.data.player.MinecraftGameProfile;

@RequiredArgsConstructor
public final class MinecraftPlayerInfo {

    @Getter
    private final MinecraftGameProfile gameProfile;
}
