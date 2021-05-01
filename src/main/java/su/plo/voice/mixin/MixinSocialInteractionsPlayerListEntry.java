package su.plo.voice.mixin;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
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
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import su.plo.voice.client.VoiceClient;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mixin(SocialInteractionsPlayerListEntry.class)
public abstract class MixinSocialInteractionsPlayerListEntry extends ElementListWidget.Entry<SocialInteractionsPlayerListEntry> {
    @Shadow @Final private MinecraftClient client;
    @Shadow @Final private UUID uuid;

    @Shadow @Final private List<Element> buttons;
    @Shadow private boolean offline;
    private ButtonWidget muteShowButton;
    private ButtonWidget muteHideButton;
    private List<Element> customButtons = ImmutableList.of();

    private static final Text field_26905;
    private static final Text field_26906;
    private static final Text field_26907;
    private static final Text field_26908;
    private static final Text field_26909;

    @Inject(method = "<init>*", at = @At("RETURN"))
    private void onConstructed(CallbackInfo ci) {
        if(VoiceClient.serverConfig == null) {
            return;
        }

        SocialInteractionsManager socialInteractionsManager = client.getSocialInteractionsManager();

        if (!client.player.getGameProfile().getId().equals(uuid) &&
                !socialInteractionsManager.isPlayerBlocked(uuid) &&
                VoiceClient.serverConfig.clients.contains(uuid)) {
            this.muteHideButton = new TexturedButtonWidget(0, 0, 20, 20, 0, 32, 20, VoiceClient.MICS, 256, 256, (buttonWidget) -> {
                VoiceClient.clientMutedClients.add(uuid);
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
                VoiceClient.clientMutedClients.remove(uuid);
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
            this.muteShowButton.visible = VoiceClient.clientMutedClients.contains(uuid);
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
    private Text getStatusText() {
        boolean bl = this.client.getSocialInteractionsManager().isPlayerHidden(this.uuid) || VoiceClient.clientMutedClients.contains(this.uuid);
        boolean bl2 = this.client.getSocialInteractionsManager().isPlayerBlocked(this.uuid);
        if (bl2 && this.offline) {
            return field_26909;
        } else if (bl && this.offline) {
            return field_26908;
        } else if (bl2) {
            return field_26906;
        } else if (bl) {
            return field_26905;
        } else {
            return this.offline ? field_26907 : LiteralText.EMPTY;
        }
    }

    /**
     * @author Apehum
     */
    @Overwrite
    public List<? extends Element> children() {
        return Stream.concat(this.buttons.stream(), this.customButtons.stream())
                .collect(Collectors.toList());
    }

    static {
        field_26905 = (new TranslatableText("gui.socialInteractions.status_hidden")).formatted(Formatting.ITALIC);
        field_26906 = (new TranslatableText("gui.socialInteractions.status_blocked")).formatted(Formatting.ITALIC);
        field_26907 = (new TranslatableText("gui.socialInteractions.status_offline")).formatted(Formatting.ITALIC);
        field_26908 = (new TranslatableText("gui.socialInteractions.status_hidden_offline")).formatted(Formatting.ITALIC);
        field_26909 = (new TranslatableText("gui.socialInteractions.status_blocked_offline")).formatted(Formatting.ITALIC);
    }
}
