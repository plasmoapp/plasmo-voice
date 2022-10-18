package su.plo.lib.mod.client.locale;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.chat.MinecraftTextComponent;
import su.plo.lib.api.client.locale.MinecraftLanguage;
import su.plo.voice.api.client.config.keybind.KeyBinding;

public final class ModLanguageWrapper implements MinecraftLanguage {

    @Override
    public @NotNull String getOrDefault(@NotNull String key) {
        Language language = Language.getInstance();
        return language.getOrDefault(key);
    }

    @Override
    public boolean has(@NotNull String key) {
        Language language = Language.getInstance();
        return language.has(key);
    }

    @Override
    public @NotNull MinecraftTextComponent getKeyDisplayName(@NotNull KeyBinding.Key key) {
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
