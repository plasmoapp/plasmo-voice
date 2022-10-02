package su.plo.lib.client.render;

import com.mojang.math.Quaternion;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
public final class ModQuaternion implements MinecraftQuaternion {

    @Getter
    @Setter
    private Quaternion instance;
}
