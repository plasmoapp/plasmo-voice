package su.plo.voice.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.Team;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderNameplateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import su.plo.voice.Voice;
import su.plo.voice.gui.VoiceHud;
import su.plo.voice.render.CustomEntityRenderer;

public class RenderEvent {
    private final VoiceHud voiceHud = new VoiceHud();
    private final Minecraft minecraft;

    public RenderEvent() {
        this.minecraft = Minecraft.getInstance();
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Pre event) {
        if (!event.getType().equals(RenderGameOverlayEvent.ElementType.CHAT)) {
            return;
        }

        this.voiceHud.render();
    }

    @SubscribeEvent
    public void onRenderName(RenderNameplateEvent event) {
        if(!Voice.connected()) {
            return;
        }
        if (!(event.getEntity() instanceof PlayerEntity)) {
            return;
        }
        if (event.getEntity() == minecraft.player) {
            return;
        }

        double d = event.getEntityRenderer().getDispatcher().distanceToSqr(event.getEntity());
        if (d > 4096.0D) {
            return;
        }

        PlayerRenderer renderer = (PlayerRenderer) event.getEntityRenderer();
        PlayerEntity player = (PlayerEntity) event.getEntity();

        CustomEntityRenderer.entityRender((PlayerEntity) event.getEntity(),
                d,
                event.getMatrixStack(),
                shouldShowName(renderer, player),
                event.getRenderTypeBuffer(), event.getPackedLight());
    }

    protected boolean shouldShowName(PlayerRenderer renderer, PlayerEntity p_177070_1_) {
        double d0 = renderer.getDispatcher().distanceToSqr(p_177070_1_);
        float f = p_177070_1_.isDiscrete() ? 32.0F : 64.0F;
        if (d0 >= (double)(f * f)) {
            return false;
        } else {
            Minecraft minecraft = Minecraft.getInstance();
            ClientPlayerEntity clientplayerentity = minecraft.player;
            boolean flag = !p_177070_1_.isInvisibleTo(clientplayerentity);
            if (p_177070_1_ != clientplayerentity) {
                Team team = p_177070_1_.getTeam();
                Team team1 = clientplayerentity.getTeam();
                if (team != null) {
                    Team.Visible team$visible = team.getNameTagVisibility();
                    switch(team$visible) {
                        case ALWAYS:
                            return flag;
                        case NEVER:
                            return false;
                        case HIDE_FOR_OTHER_TEAMS:
                            return team1 == null ? flag : team.isAlliedTo(team1) && (team.canSeeFriendlyInvisibles() || flag);
                        case HIDE_FOR_OWN_TEAM:
                            return team1 == null ? flag : !team.isAlliedTo(team1) && flag;
                        default:
                            return true;
                    }
                }
            }

            return Minecraft.renderNames() && p_177070_1_ != minecraft.getCameraEntity() && flag && !p_177070_1_.isVehicle();
        }
    }
}
