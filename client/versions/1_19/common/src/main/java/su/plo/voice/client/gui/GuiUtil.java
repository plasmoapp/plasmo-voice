package su.plo.voice.client.gui;

import com.google.common.collect.ImmutableList;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.audio.device.AudioDevice;
import su.plo.voice.api.client.audio.device.DeviceFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public final class GuiUtil {

    private static final int MAX_TOOLTIP_LINES = 32;

    public static Component getSettingsTitle(PlasmoVoiceClient voiceClient) {
        String[] versionSplit = voiceClient.getVersion().split("-");

        String version = versionSplit[0]; // remove -* from version
        ChatFormatting versionColor = ChatFormatting.WHITE;
        if (versionSplit.length > 1) {
            if (Objects.equals(versionSplit[1], "ALPHA")) {
                versionColor = ChatFormatting.YELLOW;
            } else {
                versionColor = ChatFormatting.RED;
            }
        }

        Component title = Component.translatable(
                "gui.plasmovoice.title",
                Component.literal("Plasmo Voice"),
                Component.literal(version).withStyle(versionColor)
        );
        Language language = Language.getInstance();

        if (language.getOrDefault("gui.plasmovoice.title").split("%s").length != 3) {
            return Component.literal("Plasmo Voice ")
                    .append(Component.literal(version).withStyle(versionColor))
                    .append(Component.literal(" Settings"));
        }

        return title;
    }

    public static List<Component> getVersionTooltip(PlasmoVoiceClient voiceClient) {
        String[] versionSplit = voiceClient.getVersion().split("-");

        if (versionSplit.length < 2) return null;

        if (Objects.equals(versionSplit[1], "ALPHA")) {
            return ImmutableList.of(Component.literal("Plasmo Voice Alpha Branch"));
        } else {
            return ImmutableList.of(Component.literal("Plasmo Voice Dev Branch"));
        }
    }

    public static FormattedCharSequence getOrderedText(Font textRenderer, Component text, int width) {
        int i = textRenderer.width(text);
        if (i > width) {
            FormattedText stringVisitable = FormattedText.composite(textRenderer.substrByWidth(text, width - textRenderer.width("...")), FormattedText.of("..."));
            return Language.getInstance().getVisualOrder(stringVisitable);
        } else {
            return text.getVisualOrderText();
        }
    }

    public static List<Component> multiLineTooltip(@NotNull String translation) {
        Language language = Language.getInstance();

        if (language.has(translation))
            return ImmutableList.of(Component.translatable(translation));

        List<Component> list = new ArrayList<>();
        for (int i = 1; i <= MAX_TOOLTIP_LINES; i++) {
            String line = translation + "_" + i;
            if (!language.has(line)) break;

            list.add(Component.translatable(translation + "_" + i));
        }

        return list;
    }

    public static Component formatDeviceName(@Nullable AudioDevice device, @NotNull DeviceFactory deviceFactory) {
        if (device == null)
            return Component.translatable("gui.plasmovoice.devices.not_available");

        return formatDeviceName(device.getName(), deviceFactory);
    }

    public static Component formatDeviceName(@Nullable String deviceName, @NotNull DeviceFactory deviceFactory) {
        if (deviceName == null)
            return formatDeviceName(deviceFactory.getDefaultDeviceName(), deviceFactory);

        return deviceName.startsWith("OpenAL Soft on ")
                ? Component.literal(deviceName.substring(SoundEngine.OPEN_AL_SOFT_PREFIX_LENGTH))
                : Component.literal(deviceName);
    }

    public static List<Component> formatDeviceNames(Collection<String> deviceNames, @NotNull DeviceFactory deviceFactory) {
        List<Component> list = new ArrayList<>();
        for (String deviceName : deviceNames) {
            list.add(formatDeviceName(deviceName, deviceFactory));
        }

        return list;
    }


    private GuiUtil() {
    }
}
