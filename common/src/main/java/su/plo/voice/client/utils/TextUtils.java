package su.plo.voice.client.utils;

import net.minecraft.client.gui.Font;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextUtils {
    private static final Pattern DEVICE_NAME = Pattern.compile("^(?:OpenAL.+ on )?(.*)$");

    public static List<Component> multiLine(String translation, int lines) {
        List<Component> list = new ArrayList<>();
        for (int i = 1; i < (lines + 1); i++) {
            list.add(Component.translatable(translation + "_" + i));
        }

        return list;
    }

    public static Component formatAlDeviceName(String deviceName) {
        if (deviceName == null) {
            return Component.translatable("gui.plasmo_voice.general.not_available");
        }

        Matcher matcher = DEVICE_NAME.matcher(deviceName);
        if (!matcher.matches()) {
            return Component.literal(deviceName);
        }
        return Component.literal(matcher.group(1));
    }

    public static List<Component> formatAlDeviceNames(List<String> elements) {
        List<Component> list = new ArrayList<>();
        for (String element : elements) {
            list.add(formatAlDeviceName(element));
        }

        return list;
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
}
