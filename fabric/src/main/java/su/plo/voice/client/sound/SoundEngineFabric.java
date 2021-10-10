package su.plo.voice.client.sound;

import su.plo.voice.client.sound.openal.CustomSoundEngine;
import su.plo.voice.client.sound.openal.CustomSource;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class SoundEngineFabric extends CustomSoundEngine {
    public static Method soundPhysicsPlaySound;

    @Override
    public CustomSource createSource() {
        return SourceFabric.create();
    }

    @Override
    public void postInit() {
        try {
            Class clazz = Class.forName("com.sonicether.soundphysics.SoundPhysics");
            clazz.getMethod("init").invoke(null);
            soundPhysicsPlaySound = clazz.getMethod(
                    "onPlaySound",
                    Double.class, Double.class, Double.class, Integer.class
            );

            soundPhysics = true;
        } catch (ClassNotFoundException | NoSuchMethodException |
                InvocationTargetException | IllegalAccessException ignored) {
        }
    }
}
