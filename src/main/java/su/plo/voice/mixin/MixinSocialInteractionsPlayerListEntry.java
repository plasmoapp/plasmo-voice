package su.plo.voice.mixin;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.hud.BackgroundHelper;
import net.minecraft.client.gui.screen.multiplayer.SocialInteractionsPlayerListEntry;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.network.SocialInteractionsManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import su.plo.voice.client.VoiceClient;
import su.plo.voice.gui.PlayerVolumeWidget;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.minecraft.client.gui.screen.multiplayer.SocialInteractionsPlayerListEntry.DARK_GRAY_COLOR;

@Mixin(SocialInteractionsPlayerListEntry.class)
public abstract class MixinSocialInteractionsPlayerListEntry extends ElementListWidget.Entry<SocialInteractionsPlayerListEntry> {
    @Shadow @Final private MinecraftClient client;
    @Shadow @Final private UUID uuid;

    @Shadow @Final private List<Element> buttons;
    @Shadow private boolean offline;

    @Shadow protected abstract Text getStatusText();

    private ButtonWidget muteShowButton;
    private ButtonWidget muteHideButton;
    private ButtonWidget volumeButton;
    private ButtonWidget volumeButtonActive;
    private PlayerVolumeWidget playerVolumeWidget;
    private List<Element> customButtons = ImmutableList.of();

    private static final Text field_26905;
    private static final Text field_26906;
    private static final Text field_26907;
    private static final Text field_26908;
    private static final Text field_26909;

    @Inject(method = "<init>*", at = @At("RETURN"))
    private void onConstructed(CallbackInfo ci) {
        if(VoiceClient.getServerConfig() == null) {
            return;
        }

        SocialInteractionsManager socialInteractionsManager = client.getSocialInteractionsManager();
        this.playerVolumeWidget = new PlayerVolumeWidget(uuid);
        this.playerVolumeWidget.visible = false;

        if (!client.player.getGameProfile().getId().equals(uuid) &&
                !socialInteractionsManager.isPlayerBlocked(uuid) &&
                VoiceClient.getServerConfig().getClients().contains(uuid)) {


            this.volumeButton = new TexturedButtonWidget(0, 0, 20, 20, 0, 72, 20, VoiceClient.MICS, 256, 256, (buttonWidget) -> {
                this.playerVolumeWidget.visible = true;
                this.volumeButton.visible = false;
                this.volumeButtonActive.visible = true;
            }, (buttonWidget, matrixStack, i, j) -> {}, new TranslatableText("gui.socialInteractions.hide"));

            this.volumeButtonActive = new TexturedButtonWidget(0, 0, 20, 20, 0, 92, 0, VoiceClient.MICS, 256, 256, (buttonWidget) -> {
                this.playerVolumeWidget.visible = false;
                this.volumeButtonActive.visible = false;
                this.volumeButton.visible = true;
            }, (buttonWidget, matrixStack, i, j) -> {}, new TranslatableText("gui.socialInteractions.hide"));

            this.muteHideButton = new TexturedButtonWidget(0, 0, 20, 20, 0, 32, 20, VoiceClient.MICS, 256, 256, (buttonWidget) -> {
                VoiceClient.getClientConfig().mute(uuid);
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

            }, new TranslatableText("gui.socialInteractions.hide"));
            this.muteShowButton = new TexturedButtonWidget(0, 0, 20, 20, 20, 32, 20, VoiceClient.MICS, 256, 256, (buttonWidget) -> {
                VoiceClient.getClientConfig().unmute(uuid);
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

            }, new TranslatableText("gui.socialInteractions.show"));
            this.muteShowButton.visible = VoiceClient.getClientConfig().isMuted(uuid);
            this.muteHideButton.visible = !this.muteShowButton.visible;

            this.customButtons = ImmutableList.of(this.muteHideButton, this.muteShowButton,
                    this.volumeButton, this.volumeButtonActive, this.playerVolumeWidget);
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

            if(this.playerVolumeWidget.visible) {
                this.volumeButtonActive.x = x + (entryWidth - this.volumeButtonActive.getWidth() - 52);
                this.volumeButtonActive.y = y + (entryHeight - this.volumeButtonActive.getHeight()) / 2;
                this.volumeButtonActive.render(matrices, mouseX, mouseY, tickDelta);
            } else {
                this.volumeButton.x = x + (entryWidth - this.volumeButton.getWidth() - 52);
                this.volumeButton.y = y + (entryHeight - this.volumeButton.getHeight()) / 2;
                this.volumeButton.render(matrices, mouseX, mouseY, tickDelta);
            }

            if(this.playerVolumeWidget.visible && getStatusText() != LiteralText.EMPTY) {
                DrawableHelper.fill(matrices, x + entryHeight - 2, y, x + (entryWidth / 2) + 2, y + entryHeight, DARK_GRAY_COLOR);
            }

            this.playerVolumeWidget.render(matrices, mouseX, mouseY, tickDelta, x, y, entryWidth, entryHeight);
        }
    }

    @Inject(method = "getStatusText", at = @At(value = "RETURN"), cancellable = true)
    private void getStatusText(CallbackInfoReturnable<Text> cir) {
        boolean bl = this.client.getSocialInteractionsManager().isPlayerHidden(this.uuid) ||
                VoiceClient.getClientConfig().isMuted(this.uuid);
        boolean bl2 = this.client.getSocialInteractionsManager().isPlayerBlocked(this.uuid);
        if (bl2 && this.offline) {
            cir.setReturnValue(field_26909);
        } else if (bl && this.offline) {
            cir.setReturnValue(field_26908);
        } else if (bl2) {
            cir.setReturnValue(field_26906);
        } else if (bl) {
            cir.setReturnValue(field_26905);
        } else {
            if(this.offline) {
                cir.setReturnValue(field_26907);
            } else {
                cir.setReturnValue(LiteralText.EMPTY);
            }
        }
    }

    @Inject(method = "children", at = @At(value = "RETURN"), cancellable = true)
    public void children(CallbackInfoReturnable<List<? extends Element>> cir) {
        cir.setReturnValue(Stream.concat(this.buttons.stream(), this.customButtons.stream())
                .collect(Collectors.toList()));
    }

    static {
        field_26905 = (new TranslatableText("gui.socialInteractions.status_hidden")).formatted(Formatting.ITALIC);
        field_26906 = (new TranslatableText("gui.socialInteractions.status_blocked")).formatted(Formatting.ITALIC);
        field_26907 = (new TranslatableText("gui.socialInteractions.status_offline")).formatted(Formatting.ITALIC);
        field_26908 = (new TranslatableText("gui.socialInteractions.status_hidden_offline")).formatted(Formatting.ITALIC);
        field_26909 = (new TranslatableText("gui.socialInteractions.status_blocked_offline")).formatted(Formatting.ITALIC);
    }
}
