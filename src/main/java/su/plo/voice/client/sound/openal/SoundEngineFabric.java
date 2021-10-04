package su.plo.voice.client.sound.openal;

import com.sonicether.soundphysics.SoundPhysics;

public class SoundEngineFabric extends CustomSoundEngine {
    @Override
    public CustomSource createSource() {
        return SourceFabric.create();
    }

    @Override
    public void postInit() {
        try {
            Class.forName("com.sonicether.soundphysics.SoundPhysics");
            SoundPhysics.init();
            soundPhysics = true;
        } catch (ClassNotFoundException ignored) {
        }
    }
}
