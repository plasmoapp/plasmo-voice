package su.plo.voice.client.mixin;

import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.client.resources.language.LanguageManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import su.plo.voice.client.ModVoiceClient;
import su.plo.voice.client.event.language.LanguageChangedEvent;

@Mixin(LanguageManager.class)
public abstract class MixinLanguageManager {

    @Inject(method = "setSelected", at = @At("HEAD"))
    public void setSelected(LanguageInfo languageInfo, CallbackInfo ci) {
        ModVoiceClient.INSTANCE.getEventBus().call(
                new LanguageChangedEvent(languageInfo.getCode())
        );
    }
}
