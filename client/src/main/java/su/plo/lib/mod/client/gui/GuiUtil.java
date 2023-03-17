package su.plo.lib.mod.client.gui;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.lib.api.chat.MinecraftTextComponent;
import su.plo.voice.api.client.audio.device.AudioDevice;
import su.plo.voice.api.client.audio.device.DeviceFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class GuiUtil {

    private static final int OPEN_AL_SOFT_PREFIX_LENGTH = "OpenAL Soft on ".length();

    public static MinecraftTextComponent formatDeviceName(@Nullable AudioDevice device, @NotNull DeviceFactory deviceFactory) {
        if (device == null)
            return MinecraftTextComponent.translatable("gui.plasmovoice.devices.not_available");

        return formatDeviceName(device.getName(), deviceFactory);
    }

    public static MinecraftTextComponent formatDeviceName(@Nullable String deviceName, @NotNull DeviceFactory deviceFactory) {
        if (deviceName == null)
            return formatDeviceName(deviceFactory.getDefaultDeviceName(), deviceFactory);

        return deviceName.startsWith("OpenAL Soft on ")
                ? MinecraftTextComponent.literal(deviceName.substring(OPEN_AL_SOFT_PREFIX_LENGTH))
                : MinecraftTextComponent.literal(deviceName);
    }

    public static List<MinecraftTextComponent> formatDeviceNames(Collection<String> deviceNames, @NotNull DeviceFactory deviceFactory) {
        List<MinecraftTextComponent> list = new ArrayList<>();
        for (String deviceName : deviceNames) {
            list.add(formatDeviceName(deviceName, deviceFactory));
        }

        return list;
    }

    private GuiUtil() {
    }
}
