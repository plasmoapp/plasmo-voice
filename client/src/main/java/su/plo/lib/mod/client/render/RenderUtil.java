package su.plo.lib.mod.client.render;

import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import su.plo.lib.mod.client.chat.ClientTextConverter;
import su.plo.slib.api.chat.component.McTextComponent;

import java.util.List;

@UtilityClass
public class RenderUtil {

    private static final ClientTextConverter TEXT_CONVERTER = new ClientTextConverter();

    public static int getStringX(String string, int x, boolean dropShadow) {
        return x + getStringWidth(string) + (dropShadow ? 1 : 0);
    }

    public static int getStringX(FormattedCharSequence text, int x, boolean dropShadow) {
        return x + getTextWidth(text) + (dropShadow ? 1 : 0);
    }

    public static String stringToWidth(String string, int width, boolean tail) {
        return stringToWidth(string, width, tail, "...");
    }

    public static String stringToWidth(String string, int width, boolean tail, String trimmedTextSuffix) {
        Font font = Minecraft.getInstance().font;
        if (font.width(string) <= width) return string;

        String trimmed = font.plainSubstrByWidth(string, width - font.width(trimmedTextSuffix), tail);
        return tail ? trimmedTextSuffix + trimmed : trimmed + trimmedTextSuffix;
    }

    public static String stringToWidth(String string, int width) {
        return stringToWidth(string, width, false);
    }

    public static int getTextWidth(McTextComponent text) {
        return Minecraft.getInstance().font.width(getTextConverter().convert(text));
    }

    public static int getTextWidth(FormattedCharSequence text) {
        return Minecraft.getInstance().font.width(text);
    }

    public static FormattedCharSequence getVisualOrderText(McTextComponent text) {
        return getTextConverter().convert(text).getVisualOrderText();
    }

    public static FormattedCharSequence getTruncatedText(McTextComponent text, int width) {
        Font font = Minecraft.getInstance().font;
        Component component = getTextConverter().convert(text);
        if (font.width(component) <= width) return component.getVisualOrderText();

        FormattedText truncated = font.substrByWidth(component, width - font.width(CommonComponents.ELLIPSIS));
        return Language.getInstance().getVisualOrder(FormattedText.composite(truncated, CommonComponents.ELLIPSIS));
    }

    public static List<FormattedCharSequence> splitLines(McTextComponent text, int width) {
        return Minecraft.getInstance().font.split(getTextConverter().convert(text), width);
    }

    public static int getStringWidth(String string) {
        return Minecraft.getInstance().font.width(string);
    }

    public static int getFontHeight() {
        return Minecraft.getInstance().font.lineHeight;
    }

    public static ClientTextConverter getTextConverter() {
        return TEXT_CONVERTER;
    }
}
