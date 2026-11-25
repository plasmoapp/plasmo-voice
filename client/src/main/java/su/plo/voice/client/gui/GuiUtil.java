package su.plo.voice.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.slib.api.chat.component.McTextComponent;
import su.plo.voice.api.client.audio.device.AudioDevice;
import su.plo.voice.api.client.audio.device.DeviceFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class GuiUtil {

    private static final int OPEN_AL_SOFT_PREFIX_LENGTH = "OpenAL Soft on ".length();

    public static McTextComponent formatDeviceName(@Nullable AudioDevice device, @NotNull DeviceFactory deviceFactory) {
        if (device == null)
            return McTextComponent.translatable("gui.plasmovoice.devices.not_available");

        return formatDeviceName(device.getName(), deviceFactory);
    }

    public static McTextComponent formatDeviceName(@Nullable String deviceName, @NotNull DeviceFactory deviceFactory) {
        if (deviceName == null)
            return formatDeviceName(deviceFactory.getDefaultDeviceName(), deviceFactory);

        return deviceName.startsWith("OpenAL Soft on ")
                ? McTextComponent.literal(deviceName.substring(OPEN_AL_SOFT_PREFIX_LENGTH))
                : McTextComponent.literal(deviceName);
    }

    public static List<McTextComponent> formatDeviceNames(Collection<String> deviceNames, @NotNull DeviceFactory deviceFactory) {
        List<McTextComponent> list = new ArrayList<>();
        for (String deviceName : deviceNames) {
            list.add(formatDeviceName(deviceName, deviceFactory));
        }

        return list;
    }

    public static void playUiClickSound() {
        SoundManager soundManager = Minecraft.getInstance().getSoundManager();
        soundManager.play(
                SimpleSoundInstance.forUI(
                        //#if MC>=26.4
                        //$$ SoundEvents.UI_BUTTON_CLICK,
                        //#else
                        SoundEvents.UI_BUTTON_CLICK.value(),
                        //#endif
                        1.0f,
                        0.25f
                )
        );
    }

    private GuiUtil() {
    }
}
