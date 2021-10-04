package su.plo.voice.client.sound.openal;

import com.sonicether.soundphysics.SoundPhysics;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.openal.AL10;
import su.plo.voice.client.VoiceClient;

public class SourceFabric extends CustomSource {
    private long lastEnvCalculated;
    private Vec3 lastEnvPos;

    protected SourceFabric(int pointer) {
        super(pointer);
    }

    @Nullable
    static SourceFabric create() {
        int[] is = new int[1];
        AL10.alGenSources(is);

        return AlUtil.checkErrors("Allocate new source") ? null : new SourceFabric(is[0]);
    }

    @Override
    public void prePlay() {
        if (pos == null) {
            return;
        }

        if (VoiceClient.getSoundEngine().isSoundPhysics()) {
            if (System.currentTimeMillis() - lastEnvCalculated > 1000 ||
                    (lastEnvPos != null && lastEnvPos.distanceTo(pos) > 1)) {
                SoundPhysics.onPlaySound(pos.x(), pos.y(), pos.z(), pointer);
                lastEnvPos = pos;
                lastEnvCalculated = System.currentTimeMillis();
            }
        }
    }
}
