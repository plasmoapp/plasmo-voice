package su.plo.lib.client.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

public final class ModSoundManager implements MinecraftSoundManager {

    private static final Minecraft minecraft = Minecraft.getInstance();

    @Override
    public void playSound(@NotNull Category category, @NotNull String soundId, float pitch) {
        playSound(category, soundId, pitch, 0.25F);
    }

    @Override
    public void playSound(@NotNull Category category, @NotNull String soundId, float pitch, float volume) {
        SoundEvent soundEvent;
        Field field;
        try {
            field = SoundEvents.class.getField(soundId);
            soundEvent = (SoundEvent) field.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalArgumentException("Sound not found");
        }


        switch (category) {
            case UI -> {
                minecraft.getSoundManager().play(SimpleSoundInstance.forUI(soundEvent, pitch, volume));
            }
        }
    }
}
