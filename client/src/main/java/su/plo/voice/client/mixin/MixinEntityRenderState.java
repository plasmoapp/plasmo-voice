package su.plo.voice.client.mixin;

//#if MC>=12102
//$$ import net.minecraft.client.renderer.entity.state.EntityRenderState;
//$$ import org.spongepowered.asm.mixin.Mixin;
//$$ import org.spongepowered.asm.mixin.Unique;
//$$ import su.plo.voice.client.render.voice.EntityVoiceIconState;
//$$ import su.plo.voice.client.render.EntityRenderStateAccessor;
//$$
//$$ @Mixin(EntityRenderState.class)
//$$ public class MixinEntityRenderState implements EntityRenderStateAccessor {
//$$
//$$     @Unique
//$$     private EntityVoiceIconState entityVoiceIconState;
//$$
//$$     @Override
//$$     public EntityVoiceIconState plasmovoice_getEntityVoiceIconState() {
//$$         return entityVoiceIconState;
//$$     }
//$$
//$$     @Override
//$$     public void plasmovoice_setEntityVoiceIcon(EntityVoiceIconState entityVoiceIconState) {
//$$         this.entityVoiceIconState = entityVoiceIconState;
//$$     }
//$$ }
//#endif
