package su.plo.lib.api.client.gui;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.lib.api.chat.MinecraftTextComponent;
import su.plo.lib.api.client.locale.MinecraftLanguage;
import su.plo.voice.api.client.audio.device.AudioDevice;
import su.plo.voice.api.client.audio.device.DeviceFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class GuiUtil {

    private static final int MAX_TOOLTIP_LINES = 32;
    private static final int OPEN_AL_SOFT_PREFIX_LENGTH = "OpenAL Soft on ".length();

    public static List<MinecraftTextComponent> multiLineTooltip(@NotNull MinecraftLanguage language,
                                                                @Nullable String translation) {
        if (translation == null) return Collections.emptyList();

        if (language.has(translation))
            return ImmutableList.of(MinecraftTextComponent.translatable(translation));

        List<MinecraftTextComponent> list = new ArrayList<>();
        for (int i = 1; i <= MAX_TOOLTIP_LINES; i++) {
            String line = translation + "_" + i;
            if (!language.has(line)) break;

            list.add(MinecraftTextComponent.translatable(translation + "_" + i));
        }

        return list;
    }

    public static MinecraftTextComponent formatDeviceName(@Nullable AudioDevice device, @NotNull DeviceFactory deviceFactory) {
        if (device == null)
            return MinecraftTextComponent.translatable("gui.plasmovoice.devices.not_available");

        return formatDeviceName(device.getName(), deviceFactory);
    }

    public static MinecraftTextComponent formatDeviceName(@Nullable String deviceName, @NotNull DeviceFactory deviceFactory) {
        if (deviceName == null)
            return formatDeviceName(deviceFactory.getDefaultDeviceName(), deviceFactory);

        return deviceName.startsWith("OpenAL Soft on ")
                ? MinecraftTextComponent.literal(deviceName.substring("OpenAL Soft on ".length()))
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
