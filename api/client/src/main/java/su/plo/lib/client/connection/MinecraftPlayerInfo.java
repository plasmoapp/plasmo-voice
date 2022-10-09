package su.plo.lib.client.connection;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import su.plo.lib.profile.MinecraftGameProfile;

@RequiredArgsConstructor
public final class MinecraftPlayerInfo {

    @Getter
    private final MinecraftGameProfile gameProfile;
}
