package su.plo.voice.lib.client.render;

import com.mojang.math.Quaternion;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import su.plo.lib.client.render.MinecraftQuaternion;

@RequiredArgsConstructor
public final class ModQuaternion implements MinecraftQuaternion {

    @Getter
    @Setter
    private Quaternion instance;
}
