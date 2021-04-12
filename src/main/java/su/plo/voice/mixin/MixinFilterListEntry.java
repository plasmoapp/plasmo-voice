package su.plo.voice.mixin;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.social.FilterListEntry;
import net.minecraft.client.gui.social.FilterManager;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import su.plo.voice.Voice;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mixin(FilterListEntry.class)
public abstract class MixinFilterListEntry {

    @Shadow @Final private Minecraft minecraft;
    @Shadow @Final private List<IGuiEventListener> children;
    @Shadow @Final private UUID id;
    @Shadow private boolean isRemoved;
    private Button muteShowButton;
    private Button muteHideButton;
    private List<Widget> customButtons;

    private static final ITextComponent HIDDEN = (new TranslationTextComponent("gui.socialInteractions.status_hidden")).withStyle(TextFormatting.ITALIC);
    private static final ITextComponent BLOCKED = (new TranslationTextComponent("gui.socialInteractions.status_blocked")).withStyle(TextFormatting.ITALIC);
    private static final ITextComponent OFFLINE = (new TranslationTextComponent("gui.socialInteractions.status_offline")).withStyle(TextFormatting.ITALIC);
    private static final ITextComponent HIDDEN_OFFLINE = (new TranslationTextComponent("gui.socialInteractions.status_hidden_offline")).withStyle(TextFormatting.ITALIC);
    private static final ITextComponent BLOCKED_OFFLINE = (new TranslationTextComponent("gui.socialInteractions.status_blocked_offline")).withStyle(TextFormatting.ITALIC);

    @Inject(method = "<init>*", at = @At("RETURN"))
    private void onConstructed(CallbackInfo ci) {
        if(Voice.serverConfig == null) {
            return;
        }

        FilterManager socialInteractionsManager = minecraft.getPlayerSocialManager();

        if (!minecraft.player.getGameProfile().getId().equals(id) &&
                !socialInteractionsManager.isBlocked(id) &&
                Voice.serverConfig.clients.contains(id)) {
            this.muteHideButton = new ImageButton(0, 0, 20, 20, 0, 32, 20, Voice.MICS, 256, 256, (buttonWidget) -> {
                Voice.clientMutedClients.add(id);
                this.muteShowButton.visible = true;
                this.muteHideButton.visible = false;
            }, (buttonWidget, matrixStack, i, j) -> {
                // todo tooltips
//                this.field_26864 += client.getLastFrameDuration();
//                if (this.field_26864 >= 10.0F) {
//                    parent.method_31354(() -> {
//                        this.method_31328();
//                        method_31328(parent, matrixStack, this.hideTooltip, i, j);
//                    });
//                }

            }, new TranslationTextComponent("gui.socialInteractions.hide"));
            this.muteShowButton = new ImageButton(0, 0, 20, 20, 20, 32, 20, Voice.MICS, 256, 256, (buttonWidget) -> {
                Voice.clientMutedClients.remove(id);
                this.muteShowButton.visible = false;
                this.muteHideButton.visible = true;
            }, (buttonWidget, matrixStack, i, j) -> {
                // todo tooltips
//                this.field_26864 += client.getLastFrameDuration();
//                if (this.field_26864 >= 10.0F) {
//                    parent.method_31354(() -> {
//                        method_31328(parent, matrixStack, this.showTooltip, i, j);
//                    });
//                }

            }, new TranslationTextComponent("gui.socialInteractions.show"));
            this.muteShowButton.visible = Voice.clientMutedClients.contains(id);
            this.muteHideButton.visible = !this.muteShowButton.visible;

            this.customButtons = ImmutableList.of(this.muteHideButton, this.muteShowButton);
        }
    }

    @Inject(method = "render", at = @At(value = "TAIL"))
    public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight,
                       int mouseX, int mouseY, boolean hovered, float tickDelta, CallbackInfo info) {
        if (this.muteHideButton != null && this.muteShowButton != null) {
            this.muteHideButton.x = x + (entryWidth - this.muteHideButton.getWidth() - 28);
            this.muteHideButton.y = y + (entryHeight - this.muteHideButton.getHeight()) / 2;
            this.muteHideButton.render(matrices, mouseX, mouseY, tickDelta);
            this.muteShowButton.x = x + (entryWidth - this.muteShowButton.getWidth() - 28);
            this.muteShowButton.y = y + (entryHeight - this.muteShowButton.getHeight()) / 2;
            this.muteShowButton.render(matrices, mouseX, mouseY, tickDelta);
        }
    }

    /**
     * @author Apehum
     */
    @Overwrite
    private ITextComponent getStatusComponent() {
        boolean bl = this.minecraft.getPlayerSocialManager().isHidden(this.id) || Voice.clientMutedClients.contains(this.id);
        boolean bl2 = this.minecraft.getPlayerSocialManager().isBlocked(this.id);
        if (bl2 && this.isRemoved) {
            return BLOCKED_OFFLINE;
        } else if (bl && this.isRemoved) {
            return HIDDEN_OFFLINE;
        } else if (bl2) {
            return BLOCKED;
        } else if (bl) {
            return HIDDEN;
        } else {
            return this.isRemoved ? OFFLINE : StringTextComponent.EMPTY;
        }
    }

    /**
     * @author Apehum
     */
    @Overwrite
    public List<? extends IGuiEventListener> children() {
        return Stream.concat(this.children.stream(), this.customButtons.stream())
                .collect(Collectors.toList());
    }

//    static {
//        field_26905 = (new TranslatableText("gui.socialInteractions.status_hidden")).formatted(Formatting.ITALIC);
//        field_26906 = (new TranslatableText("gui.socialInteractions.status_blocked")).formatted(Formatting.ITALIC);
//        field_26907 = (new TranslatableText("gui.socialInteractions.status_offline")).formatted(Formatting.ITALIC);
//        field_26908 = (new TranslatableText("gui.socialInteractions.status_hidden_offline")).formatted(Formatting.ITALIC);
//        field_26909 = (new TranslatableText("gui.socialInteractions.status_blocked_offline")).formatted(Formatting.ITALIC);
//    }
}
