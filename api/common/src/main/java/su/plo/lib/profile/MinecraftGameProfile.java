package su.plo.lib.profile;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Data
@RequiredArgsConstructor
public class MinecraftGameProfile {

    private final UUID id;
    private final String name;
}
