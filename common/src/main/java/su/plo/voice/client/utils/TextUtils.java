package su.plo.voice.client.utils;

import net.minecraft.client.gui.Font;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
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
            list.add(new TranslatableComponent(translation + "_" + i));
        }

        return list;
    }

    public static String formatAlDeviceName(String deviceName) {
        Matcher matcher = DEVICE_NAME.matcher(deviceName);
        if (!matcher.matches()) {
            return deviceName;
        }
        return matcher.group(1);
    }

    public static List<Component> formatAlDeviceNames(List<String> elements) {
        List<Component> list = new ArrayList<>();
        for (String element : elements) {
            list.add(new TextComponent(formatAlDeviceName(element)));
        }

        return list;
    }

    public static List<Component> stringToText(List<String> elements) {
        List<Component> list = new ArrayList<>();
        for (String element : elements) {
            list.add(new TextComponent(element));
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
