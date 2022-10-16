package su.plo.lib.mod.client.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.client.sound.MinecraftSoundManager;

import java.lang.reflect.Field;

public final class ModSoundManager implements MinecraftSoundManager {

    private static final Minecraft minecraft = Minecraft.getInstance();

    @Override
    public void playSound(@NotNull Category category, @NotNull String soundLocation, float pitch) {
        playSound(category, soundLocation, pitch, 0.25F);
    }

    @Override
    public void playSound(@NotNull Category category, @NotNull String soundLocation, float pitch, float volume) {
        SoundEvent soundEvent = null;
        for (Field field : SoundEvents.class.getFields()) {
            try {
                Object object = field.get(null);
                if (object instanceof SoundEvent fieldSound) {
                    if (fieldSound.getLocation().toString().equals(soundLocation)) {
                        soundEvent = fieldSound;
                        break;
                    }
                }
            } catch (IllegalAccessException ignored) {
            }
        }

        if (soundEvent == null) {
            LogManager.getLogger().error("Sound '" + soundLocation + "' not found");
            return;
        }

        switch (category) {
            case UI -> {
                minecraft.getSoundManager().play(SimpleSoundInstance.forUI(soundEvent, pitch, volume));
            }
        }
    }
}
