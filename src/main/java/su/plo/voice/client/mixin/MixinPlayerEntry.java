package su.plo.voice.client.mixin;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.social.PlayerEntry;
import net.minecraft.client.gui.screens.social.PlayerSocialManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import su.plo.voice.client.VoiceClient;
import su.plo.voice.client.gui.PlayerVolumeWidget;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.minecraft.client.gui.screens.social.PlayerEntry.BG_FILL_REMOVED;

@Mixin(PlayerEntry.class)
public abstract class MixinPlayerEntry {

    @Shadow @Final private Minecraft minecraft;
    @Shadow @Final private UUID id;

    @Shadow protected abstract Component getStatusComponent();

    @Shadow private boolean isRemoved;
    @Shadow @Final private List<AbstractWidget> children;
    private Button muteShowButton;
    private Button muteHideButton;
    private Button volumeButton;
    private Button volumeButtonActive;
    private PlayerVolumeWidget playerVolumeWidget;
    private List<GuiEventListener> customButtons = ImmutableList.of();

    private static final Component HIDDEN = (new TranslatableComponent("gui.socialInteractions.status_hidden")).withStyle(ChatFormatting.ITALIC);
    private static final Component BLOCKED = (new TranslatableComponent("gui.socialInteractions.status_blocked")).withStyle(ChatFormatting.ITALIC);
    private static final Component OFFLINE = (new TranslatableComponent("gui.socialInteractions.status_offline")).withStyle(ChatFormatting.ITALIC);
    private static final Component HIDDEN_OFFLINE = (new TranslatableComponent("gui.socialInteractions.status_hidden_offline")).withStyle(ChatFormatting.ITALIC);
    private static final Component BLOCKED_OFFLINE = (new TranslatableComponent("gui.socialInteractions.status_blocked_offline")).withStyle(ChatFormatting.ITALIC);

    @Inject(method = "<init>*", at = @At("RETURN"))
    private void onConstructed(CallbackInfo ci) {
        if(VoiceClient.getServerConfig() == null) {
            return;
        }

        PlayerSocialManager socialInteractionsManager = minecraft.getPlayerSocialManager();
        this.playerVolumeWidget = new PlayerVolumeWidget(id);
        this.playerVolumeWidget.visible = false;

        if (!minecraft.player.getGameProfile().getId().equals(id) &&
                !socialInteractionsManager.isBlocked(id) &&
                VoiceClient.getServerConfig().getClients().contains(id)) {


            this.volumeButton = new ImageButton(0, 0, 20, 20, 0, 72, 20, VoiceClient.ICONS, 256, 256, (buttonWidget) -> {
                this.playerVolumeWidget.visible = true;
                this.volumeButton.visible = false;
                this.volumeButtonActive.visible = true;
            }, (buttonWidget, matrixStack, i, j) -> {}, new TranslatableComponent("gui.socialInteractions.hide"));

            this.volumeButtonActive = new ImageButton(0, 0, 20, 20, 0, 92, 0, VoiceClient.ICONS, 256, 256, (buttonWidget) -> {
                this.playerVolumeWidget.visible = false;
                this.volumeButtonActive.visible = false;
                this.volumeButton.visible = true;
            }, (buttonWidget, matrixStack, i, j) -> {}, new TranslatableComponent("gui.socialInteractions.hide"));

            this.muteHideButton = new ImageButton(0, 0, 20, 20, 0, 32, 20, VoiceClient.ICONS, 256, 256, (buttonWidget) -> {
                VoiceClient.getClientConfig().mute(id);
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

            }, new TranslatableComponent("gui.socialInteractions.hide"));
            this.muteShowButton = new ImageButton(0, 0, 20, 20, 20, 32, 20, VoiceClient.ICONS, 256, 256, (buttonWidget) -> {
                VoiceClient.getClientConfig().unmute(id);
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

            }, new TranslatableComponent("gui.socialInteractions.show"));
            this.muteShowButton.visible = VoiceClient.getClientConfig().isMuted(id);
            this.muteHideButton.visible = !this.muteShowButton.visible;

            this.customButtons = ImmutableList.of(this.muteHideButton, this.muteShowButton,
                    this.volumeButton, this.volumeButtonActive, this.playerVolumeWidget);
        }
    }

    @Inject(method = "render", at = @At(value = "TAIL"))
    public void render(PoseStack matrices, int index, int y, int x, int entryWidth, int entryHeight,
                       int mouseX, int mouseY, boolean hovered, float tickDelta, CallbackInfo info) {
        if (this.muteHideButton != null && this.muteShowButton != null) {
            this.muteHideButton.x = x + (entryWidth - this.muteHideButton.getWidth() - 28);
            this.muteHideButton.y = y + (entryHeight - this.muteHideButton.getHeight()) / 2;
            this.muteHideButton.render(matrices, mouseX, mouseY, tickDelta);
            this.muteShowButton.x = x + (entryWidth - this.muteShowButton.getWidth() - 28);
            this.muteShowButton.y = y + (entryHeight - this.muteShowButton.getHeight()) / 2;
            this.muteShowButton.render(matrices, mouseX, mouseY, tickDelta);

            if(this.playerVolumeWidget.visible) {
                this.volumeButtonActive.x = x + (entryWidth - this.volumeButtonActive.getWidth() - 52);
                this.volumeButtonActive.y = y + (entryHeight - this.volumeButtonActive.getHeight()) / 2;
                this.volumeButtonActive.render(matrices, mouseX, mouseY, tickDelta);
            } else {
                this.volumeButton.x = x + (entryWidth - this.volumeButton.getWidth() - 52);
                this.volumeButton.y = y + (entryHeight - this.volumeButton.getHeight()) / 2;
                this.volumeButton.render(matrices, mouseX, mouseY, tickDelta);
            }

            if(this.playerVolumeWidget.visible && getStatusComponent() != TextComponent.EMPTY) {
                GuiComponent.fill(matrices, x + entryHeight - 2, y, x + (entryWidth / 2) + 2, y + entryHeight, BG_FILL_REMOVED);
            }

            this.playerVolumeWidget.render(matrices, mouseX, mouseY, tickDelta, x, y, entryWidth, entryHeight);
        }
    }

    @Inject(method = "getStatusComponent", at = @At(value = "RETURN"), cancellable = true)
    private void getStatusText(CallbackInfoReturnable<Component> cir) {
        boolean bl = this.minecraft.getPlayerSocialManager().isBlocked(this.id) ||
                VoiceClient.getClientConfig().isMuted(this.id   );
        boolean bl2 = this.minecraft.getPlayerSocialManager().isBlocked(this.id);
        if (bl2 && this.isRemoved) {
            cir.setReturnValue(BLOCKED_OFFLINE);
        } else if (bl && this.isRemoved) {
            cir.setReturnValue(HIDDEN_OFFLINE);
        } else if (bl2) {
            cir.setReturnValue(BLOCKED);
        } else if (bl) {
            cir.setReturnValue(HIDDEN);
        } else {
            if(this.isRemoved) {
                cir.setReturnValue(OFFLINE);
            } else {
                cir.setReturnValue(TextComponent.EMPTY);
            }
        }
    }

    @Inject(method = "children", at = @At(value = "RETURN"), cancellable = true)
    public void children(CallbackInfoReturnable<List<? extends GuiEventListener>> cir) {
        cir.setReturnValue(Stream.concat(this.children.stream(), this.customButtons.stream())
                .collect(Collectors.toList()));
    }
}
