package su.plo.lib.mod.client.gui.components;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import su.plo.lib.mod.client.Inputs;
import su.plo.lib.mod.client.gui.narration.NarrationOutput;
import su.plo.lib.mod.client.gui.widget.GuiAbstractWidget;
import su.plo.lib.mod.client.render.Colors;
import su.plo.lib.mod.client.render.RenderUtil;
import su.plo.lib.mod.client.render.gui.GuiRenderContext;
import su.plo.slib.api.chat.component.McTextComponent;

import java.awt.Color;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;

//#if MC>=12002
//$$ import su.plo.lib.mod.client.gui.widget.GuiWidgetTexture;
//#endif

//#if MC>=12109
//$$ import com.mojang.blaze3d.platform.cursor.CursorTypes;
//#endif

public class TextFieldWidget extends GuiAbstractWidget {

    @Setter
    @Nullable
    private Consumer<String> responder;
    @Setter
    private Predicate<String> filter;
    @Setter
    private BiFunction<String, Integer, McTextComponent> formatter;

    @Getter
    private String value;
    @Getter
    private int maxLength;
    private int frame;
    @Getter
    @Setter
    private boolean editable;
    @Getter
    @Setter
    private boolean bordered;
    @Getter
    @Setter
    private boolean canLoseFocus;
    private boolean shiftPressed;
    private int displayPosition;
    private int cursorPosition;
    private int highlightPosition;
    @Setter
    private Color textColor;
    @Setter
    private Color textColorUneditable;
    @Setter
    @Nullable
    private String suggestion;


    public TextFieldWidget(
            int x,
            int y,
            int width,
            int height,
            @NotNull McTextComponent text
    ) {
        super(x, y, width, height, text);

        this.value = "";
        this.maxLength = 32;
        this.bordered = true;
        this.canLoseFocus = true;
        this.editable = true;
        this.textColor = new Color(0xE0E0E0);
        this.textColorUneditable = new Color(0x707070);
        this.filter = Objects::nonNull;
        this.formatter = (string, integer) -> McTextComponent.literal(string);
    }

    @Override
    public void updateNarration(@NotNull NarrationOutput narrationOutput) {
        narrationOutput.add(NarrationOutput.Type.TITLE, McTextComponent.translatable("narration.edit_box", getValue()));
    }

    @Override
    public boolean keyPressed(int keyCode, int modifiers) {
        if (!canConsumeInput()) return false;

        this.shiftPressed = Inputs.hasShiftDown();

        if (Inputs.isSelectAll(keyCode, modifiers)) {
            moveCursorToEnd();
            setHighlightPos(0);
            return true;
        }

        if (Inputs.isCopy(keyCode, modifiers)) {
            Minecraft.getInstance().keyboardHandler.setClipboard(getHighlighted());
            return true;
        }

        if (Inputs.isPaste(keyCode, modifiers)) {
            if (isEditable()) {
                insertText(Minecraft.getInstance().keyboardHandler.getClipboard());
            }

            return true;
        }

        if (Inputs.isCut(keyCode, modifiers)) {
            Minecraft.getInstance().keyboardHandler.setClipboard(getHighlighted());
            if (isEditable()) {
                insertText("");
            }

            return true;
        }

        switch (keyCode) {
            case GLFW.GLFW_KEY_BACKSPACE:
                if (isEditable()) {
                    shiftPressed = false;
                    deleteText(modifiers, -1);
                    shiftPressed = Inputs.hasShiftDown();
                }

                return true;
            case GLFW.GLFW_KEY_INSERT:
            case GLFW.GLFW_KEY_DOWN:
            case GLFW.GLFW_KEY_UP:
            case GLFW.GLFW_KEY_PAGE_UP:
            case GLFW.GLFW_KEY_PAGE_DOWN:
            default:
                return false;
            case GLFW.GLFW_KEY_DELETE:
                if (isEditable()) {
                    this.shiftPressed = false;
                    this.deleteText(modifiers, 1);
                    this.shiftPressed = Inputs.hasShiftDown();
                }

                return true;
            case GLFW.GLFW_KEY_RIGHT:
                if (Inputs.hasControlDown(modifiers)) {
                    moveCursorTo(getWordPosition(1));
                } else {
                    moveCursor(1);
                }

                return true;
            case GLFW.GLFW_KEY_LEFT:
                if (Inputs.hasControlDown(modifiers)) {
                    moveCursorTo(this.getWordPosition(-1));
                } else {
                    moveCursor(-1);
                }

                return true;
            case GLFW.GLFW_KEY_HOME:
                moveCursorToStart();
                return true;
            case GLFW.GLFW_KEY_END:
                moveCursorToEnd();
                return true;
        }
    }

    @Override
    public boolean charTyped(char typedChar, int modifiers) {
        if (!canConsumeInput() || !isAllowedChatCharacter(typedChar)) return false;

        if (isEditable()) insertText(Character.toString(typedChar));
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isVisible()) return false;

        boolean mouseOver = isMouseOver(mouseX, mouseY);
        if (canLoseFocus) setFocused(mouseOver);

        if (isFocused() && mouseOver && isValidClickButton(button)) {
            int inputX = Mth.floor(mouseX) - x;
            if (bordered) inputX -= 4;

            String visibileString = RenderUtil.stringToWidth(value.substring(displayPosition), getInnerWidth());
            moveCursorTo(RenderUtil.stringToWidth(visibileString, inputX, false, "").length() + displayPosition);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean changeFocus(boolean lookForwards) {
        return visible && isEditable() && super.changeFocus(lookForwards);
    }

    @Override
    protected void onFocusedChanged(boolean focused) {
        if (focused) this.frame = 0;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return visible && super.isMouseOver(mouseX, mouseY);
    }

    @Override
    public void renderButton(@NotNull GuiRenderContext context, int mouseX, int mouseY, float delta) {
        if (!isVisible()) return;

        Color color;
        if (isBordered()) {
            color = isFocused() ? Colors.WHITE : Colors.GRAY;

            //#if MC>=12002
            //$$ GuiWidgetTexture sprite = isFocused() ? GuiWidgetTexture.TEXT_FIELD_ACTIVE : GuiWidgetTexture.TEXT_FIELD;
            //$$
            //$$ context.blitSprite(sprite, x, y, 0, 0, width / 2, height);
            //$$ context.blitSprite(sprite, x + width / 2, y, sprite.getSpriteWidth() - width / 2, 0, width / 2, height);
            //#else
            context.fill(x, y, x + width, y + height, color);
            context.fill(x + 1, y + 1, x + width - 1, y + height - 1, Colors.BLACK);
            //#endif
        }

        color = isEditable() ? textColor : textColorUneditable;
        int cursorIndex = cursorPosition - displayPosition;
        int selectionEnd = highlightPosition - displayPosition;
        String text = isFocused()
                ? RenderUtil.stringToWidth(value.substring(displayPosition), getInnerWidth(), false, "")
                : RenderUtil.stringToWidth(value.substring(displayPosition), getInnerWidth());

        boolean isCursorVisible = cursorIndex >= 0 && cursorIndex <= text.length();
        boolean isFocused = isFocused() && frame / 6 % 2 == 0 && isCursorVisible;
        int textX = bordered ? x + 5 : x;
        int textY = bordered ? y + (height - 8) / 2 : y;
        int currentX = textX;
        if (selectionEnd > text.length()) {
            selectionEnd = text.length();
        }
        String visibleText = isCursorVisible ? text.substring(0, cursorIndex) : text;

        if (!text.isEmpty()) {
            currentX = RenderUtil.getStringX(RenderUtil.getFormattedString(formatter.apply(visibleText, displayPosition)), textX, true);
        }

        boolean shouldDrawCursor = cursorPosition < value.length() || value.length() >= maxLength;
        int selectionX = currentX;
        if (!isCursorVisible) {
            selectionX = cursorIndex > 0 ? textX + width : textX;
        } else if (shouldDrawCursor) {
            selectionX = currentX - 1;
            --currentX;
        }

        if (selectionEnd != cursorIndex) {
            int selectionWidth = textX + RenderUtil.getStringWidth(text.substring(0, selectionEnd));
            renderHighlight(
                    context,
                    selectionX,
                    textY - 1,
                    selectionWidth - 1,
                    textY + 1 + 9
            );
        }

        if (!text.isEmpty()) {
            context.drawString(formatter.apply(visibleText, displayPosition), textX, textY, color);
        }

        if (!text.isEmpty() && isCursorVisible && cursorIndex < text.length()) {
            context.drawString(formatter.apply(text.substring(cursorIndex), cursorPosition), currentX, textY, color);
        }

        if (!shouldDrawCursor && suggestion != null) {
            context.drawString(suggestion, selectionX - 1, textY, new Color(0x808080));
        }

        if (text.isEmpty() && !isFocused()) {
            context.drawString(getText(), textX, textY, Colors.BLACK);
        }

        if (isFocused) {
            if (shouldDrawCursor) {
                context.fill(
                        selectionX,
                        textY - 1,
                        selectionX + 1,
                        textY + 1 + 9,
                        new Color(0xD0D0D0)
                );
            } else {
                context.drawString("_", selectionX, textY, color);
            }
        }

        //#if MC>=12109
        //$$ if (isHovered()) {
        //$$     context.requestCursor(CursorTypes.IBEAM);
        //$$ }
        //#endif
    }

    private void renderHighlight(@NotNull GuiRenderContext context, int x0, int y0, int x1, int y1) {
        if (x0 < x1) {
            int x = x0;
            x0 = x1;
            x1 = x;
        }

        if (y0 < y1) {
            int y = y0;
            y0 = y1;
            y1 = y;
        }

        if (x1 > x + width) {
            x1 = x + width;
        }

        if (x0 > x + width) {
            x0 = x + width;
        }

        context.fill(x0, y0, x1, y1, new Color(0x4B4BFF));
    }

    public boolean canConsumeInput() {
        return isVisible() && isFocused() && isEditable();
    }

    public void tick() {
        ++this.frame;
    }

    public void setValue(String value) {
        if (!filter.test(value)) return;

        if (value.length() > maxLength) {
            this.value = value.substring(0, maxLength);
        } else {
            this.value = value;
        }

        this.moveCursorToEnd();
        this.setHighlightPos(cursorPosition);
        this.onValueChange(value);
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
        if (value.length() > maxLength) {
            this.value = value.substring(0, maxLength);
            this.onValueChange(value);
        }
    }

    public String getHighlighted() {
        int start = Math.min(cursorPosition, highlightPosition);
        int end = Math.max(cursorPosition, highlightPosition);
        return value.substring(start, end);
    }

    public void insertText(String string) {
        int startPosition = Math.min(cursorPosition, highlightPosition);
        int endPosition = Math.max(cursorPosition, highlightPosition);
        int maxInsertLength = maxLength - value.length() - (startPosition - endPosition);
        String filteredText = filterText(string);
        int filteredTextLength = filteredText.length();
        if (maxInsertLength < filteredTextLength) {
            filteredText = filteredText.substring(0, maxInsertLength);
            filteredTextLength = maxInsertLength;
        }

        String newValue = (new StringBuilder(value)).replace(startPosition, endPosition, filteredText).toString();
        if (filter.test(newValue)) {
            this.value = newValue;
            this.setCursorPosition(startPosition + filteredTextLength);
            this.setHighlightPos(cursorPosition);
            this.onValueChange(value);
        }
    }

    private void deleteText(int modifiers, int offset) {
        if (Inputs.hasControlDown(modifiers)) {
            deleteWords(offset);
        } else {
            deleteChars(offset);
        }
    }

    public void deleteWords(int offset) {
        if (value.isEmpty()) {
            return;
        }

        if (highlightPosition != cursorPosition) {
            insertText("");
        } else {
            deleteChars(getWordPosition(offset) - cursorPosition);
        }
    }

    public void deleteChars(int offset) {
        if (value.isEmpty()) {
            return;
        }

        if (highlightPosition != cursorPosition) {
            this.insertText("");
            return;
        }

        int cursorPosition = getCursorPos(offset);
        int startRange = Math.min(cursorPosition, this.cursorPosition);
        int endRange = Math.max(cursorPosition, this.cursorPosition);
        if (startRange == endRange) {
            return;
        }

        String newText = (new StringBuilder(value)).delete(startRange, endRange).toString();
        if (filter.test(newText)) {
            this.value = newText;
            this.moveCursorTo(startRange);
        }
    }

    public int getWordPosition(int offset) {
        return this.getWordPosition(offset, cursorPosition);
    }

    private int getWordPosition(int offset, int cursorPosition) {
        int wordPosition = cursorPosition;
        boolean toLeft = offset < 0;
        int searchLimit = Math.abs(offset);

        for (int index = 0; index < searchLimit; ++index) {
            if (toLeft) {
                while (wordPosition > 0 && this.value.charAt(wordPosition - 1) == ' ') {
                    --wordPosition;
                }

                while (wordPosition > 0 && this.value.charAt(wordPosition - 1) != ' ') {
                    --wordPosition;
                }
            } else {
                int valueLength = this.value.length();
                wordPosition = this.value.indexOf(32, wordPosition);

                if (wordPosition == -1) {
                    wordPosition = valueLength;
                } else {
                    while (wordPosition < valueLength && this.value.charAt(wordPosition) == ' ') {
                        ++wordPosition;
                    }
                }
            }
        }

        return wordPosition;
    }

    public void setCursorPosition(int position) {
        this.cursorPosition = Mth.clamp(position, 0, value.length());
    }

    public void moveCursor(int offset) {
        moveCursorTo(getCursorPos(offset));
    }

    private int getCursorPos(int offset) {
        return offsetByCodepoints(value, cursorPosition, offset);
    }

    public int getInnerWidth() {
        return isBordered() ? width - 8 : width;
    }

    public int getScreenX(int x) {
        return x > value.length() ? this.x : this.x + RenderUtil.getStringWidth(value.substring(0, x));
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setHighlightPos(int position) {
        int valueLength = value.length();
        this.highlightPosition = Mth.clamp(position, 0, valueLength);

        if (displayPosition > valueLength) {
            this.displayPosition = valueLength;
        }

        int innerWidth = getInnerWidth();
        String string = RenderUtil.stringToWidth(value.substring(displayPosition), innerWidth);
        int l = string.length() + displayPosition;
        if (highlightPosition == displayPosition) {
            this.displayPosition -= RenderUtil.stringToWidth(value, innerWidth, true).length();
        }

        if (highlightPosition > l) {
            this.displayPosition += highlightPosition - l;
        } else if (highlightPosition <= displayPosition) {
            this.displayPosition -= displayPosition - highlightPosition;
        }

        this.displayPosition = Mth.clamp(displayPosition, 0, valueLength);
    }

    public void moveCursorToStart() {
        moveCursorTo(0);
    }

    public void moveCursorToEnd() {
        moveCursorTo(value.length());
    }

    public void moveCursorTo(int i) {
        setCursorPosition(i);
        if (!shiftPressed) {
            setHighlightPos(cursorPosition);
        }

        onValueChange(value);
    }

    private void onValueChange(String string) {
        if (responder != null) responder.accept(string);
    }

    @Override
    protected McTextComponent createNarrationMessage() {
        return McTextComponent.translatable("gui.narrate.editBox", getText(), value);
    }

    public static String filterText(String string) {
        StringBuilder stringBuilder = new StringBuilder();

        for (char c : string.toCharArray()) {
            if (isAllowedChatCharacter(c)) {
                stringBuilder.append(c);
            }
        }

        return stringBuilder.toString();
    }

    public static boolean isAllowedChatCharacter(char c) {
        return c != 167 && c >= ' ' && c != 127;
    }

    public static int offsetByCodepoints(String string, int cursorPosition, int offset) {
        int stringLength = string.length();
        if (offset >= 0) {
            for (int i = 0; cursorPosition < stringLength && i < offset; ++i) {
                if (Character.isHighSurrogate(string.charAt(cursorPosition++)) &&
                        cursorPosition < stringLength &&
                        Character.isLowSurrogate(string.charAt(cursorPosition))
                ) {
                    ++cursorPosition;
                }
            }
        } else {
            for (int i = offset; cursorPosition > 0 && i < 0; ++i) {
                --cursorPosition;
                if (Character.isLowSurrogate(string.charAt(cursorPosition)) &&
                        cursorPosition > 0 &&
                        Character.isHighSurrogate(string.charAt(cursorPosition - 1))
                ) {
                    --cursorPosition;
                }
            }
        }

        return cursorPosition;
    }
}
