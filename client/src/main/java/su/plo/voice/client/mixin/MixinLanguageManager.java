package su.plo.voice.client.mixin;

import net.minecraft.client.resources.language.LanguageManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import su.plo.voice.client.ModVoiceClient;
import su.plo.voice.client.event.language.LanguageChangedEvent;
import su.plo.voice.client.meta.PlasmoVoiceMeta;


import net.minecraft.client.resources.language.ClientLanguage;
import java.util.function.Consumer;

@Mixin(LanguageManager.class)
public abstract class MixinLanguageManager {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(String string, Consumer<ClientLanguage> reloadConsumer, CallbackInfo ci) {
        PlasmoVoiceMeta.Companion.fetch(string);
    }

    @Inject(method = "setSelected", at = @At("HEAD"))
    public void setSelected(String languageCode, CallbackInfo ci) {
        if (ModVoiceClient.INSTANCE == null) return;

        PlasmoVoiceMeta.Companion.fetch(languageCode);
        ModVoiceClient.INSTANCE.getEventBus().fire(
                new LanguageChangedEvent(languageCode)
        );
    }
}
