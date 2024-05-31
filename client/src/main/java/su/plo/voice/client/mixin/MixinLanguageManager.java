package su.plo.voice.client.mixin;

import net.minecraft.client.resources.language.LanguageManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import su.plo.voice.client.ModVoiceClient;
import su.plo.voice.client.event.language.LanguageChangedEvent;
import su.plo.voice.client.meta.PlasmoVoiceMeta;

//#if MC<11904
//$$ import net.minecraft.client.resources.language.LanguageInfo;
//#endif

//#if MC>=12100
//$$ import net.minecraft.client.resources.language.ClientLanguage;
//$$ import java.util.function.Consumer;
//#endif

@Mixin(LanguageManager.class)
public abstract class MixinLanguageManager {

    @Inject(method = "<init>", at = @At("RETURN"))
    //#if MC>=12100
    //$$ private void init(String string, Consumer<ClientLanguage> reloadConsumer, CallbackInfo ci) {
    //#else
    private void init(String string, CallbackInfo ci) {
    //#endif
        //#if FORGE
        //$$ try {
        //$$     Class.forName("kotlin.jvm.internal.Intrinsics");
        //$$ } catch (Exception ignored) {
        //$$     return;
        //$$ }
        //#endif

        PlasmoVoiceMeta.Companion.fetch(string);
    }

    //#if MC>=11904
    @Inject(method = "setSelected", at = @At("HEAD"))
    public void setSelected(String languageCode, CallbackInfo ci) {
        if (ModVoiceClient.INSTANCE == null) return;

        PlasmoVoiceMeta.Companion.fetch(languageCode);
        ModVoiceClient.INSTANCE.getEventBus().fire(
                new LanguageChangedEvent(languageCode)
        );
    }
    //#else
    //$$ @Inject(method = "setSelected", at = @At("HEAD"))
    //$$ public void setSelected(LanguageInfo languageInfo, CallbackInfo ci) {
    //$$     if (ModVoiceClient.INSTANCE == null) return;
    //$$
    //$$     PlasmoVoiceMeta.Companion.fetch(languageInfo.getCode());
    //$$     ModVoiceClient.INSTANCE.getEventBus().fire(
    //$$             new LanguageChangedEvent(languageInfo.getCode())
    //$$     );
    //$$ }
    //#endif
}
