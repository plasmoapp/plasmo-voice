package su.plo.lib.mod.client.language;

import com.mojang.blaze3d.platform.InputConstants;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.jetbrains.annotations.NotNull;
import su.plo.slib.api.chat.component.McTextComponent;
import su.plo.voice.api.client.config.hotkey.Hotkey;

@UtilityClass
public class LanguageUtil {

    public static String getOrDefault(String key) {
        return Language.getInstance().getOrDefault(key);
    }

    public static boolean has(String key) {
        return Language.getInstance().has(key);
    }

    public static String getSelectedLanguage() {
        //#if MC>=11904
        //$$ return Minecraft.getInstance().getLanguageManager().getSelected();
        //#else
        return Minecraft.getInstance().getLanguageManager().getSelected().getCode();
        //#endif
    }

    public @NotNull McTextComponent getKeyDisplayName(@NonNull Hotkey.Key key) {
        InputConstants.Key inputKey;

        if (key.getType() == Hotkey.Type.KEYSYM) {
            inputKey = InputConstants.Type.KEYSYM.getOrCreate(key.getCode());
        } else if (key.getType() == Hotkey.Type.MOUSE) {
            inputKey = InputConstants.Type.MOUSE.getOrCreate(key.getCode());
        } else if (key.getType() == Hotkey.Type.SCANCODE) {
            inputKey = InputConstants.Type.SCANCODE.getOrCreate(key.getCode());
        } else {
            return McTextComponent.translatable("gui.none");
        }

        Component displayName = inputKey.getDisplayName();
        //#if MC>=11900
        if (displayName.getContents() instanceof TranslatableContents) {
            TranslatableContents translatable = (TranslatableContents) displayName.getContents();
            return McTextComponent.translatable(translatable.getKey(), translatable.getArgs());
        } else if (displayName.getContents() instanceof LiteralContents) {
            LiteralContents literal = (LiteralContents) displayName.getContents();
            return McTextComponent.translatable(literal.text());
        } else {
            return McTextComponent.translatable("gui.none");
        }
        //#else
        //$$ if (displayName instanceof TranslatableComponent) {
        //$$     TranslatableComponent translatable = (TranslatableComponent) displayName;
        //$$     return McTextComponent.translatable(translatable.getKey(), translatable.getArgs());
        //$$ } else if (displayName instanceof TextComponent) {
        //$$     TextComponent literal = (TextComponent) displayName;
        //$$     return McTextComponent.translatable(literal.getText());
        //$$ } else {
        //$$     return McTextComponent.translatable("gui.none");
        //$$ }
        //#endif
    }
}
