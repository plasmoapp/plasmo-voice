package su.plo.lib.mod.client.language;

import com.mojang.blaze3d.platform.InputConstants;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.chat.MinecraftTextComponent;
import su.plo.voice.api.client.config.keybind.KeyBinding;

@UtilityClass
public class LanguageUtil {

    public static String getOrDefault(String key) {
        return Language.getInstance().getOrDefault(key);
    }

    public static boolean has(String key) {
        return Language.getInstance().has(key);
    }

    // todo: legacy
    public @NotNull MinecraftTextComponent getKeyDisplayName(@NonNull KeyBinding.Key key) {
        InputConstants.Key inputKey;

        if (key.getType() == KeyBinding.Type.KEYSYM) {
            inputKey = InputConstants.Type.KEYSYM.getOrCreate(key.getCode());
        } else if (key.getType() == KeyBinding.Type.MOUSE) {
            inputKey = InputConstants.Type.MOUSE.getOrCreate(key.getCode());
        } else if (key.getType() == KeyBinding.Type.SCANCODE) {
            inputKey = InputConstants.Type.SCANCODE.getOrCreate(key.getCode());
        } else {
            return MinecraftTextComponent.translatable("gui.none");
        }

        Component displayName = inputKey.getDisplayName();
        if (displayName.getContents() instanceof TranslatableContents translatable) {
            return MinecraftTextComponent.translatable(translatable.getKey(), translatable.getArgs());
        } else if (displayName.getContents() instanceof LiteralContents literal) {
            return MinecraftTextComponent.translatable(literal.text());
        } else {
            return MinecraftTextComponent.translatable("gui.none");
        }
    }
}
