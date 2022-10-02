package su.plo.voice.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.client.MinecraftClientLib;
import su.plo.lib.client.entity.MinecraftPlayer;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.client.entity.ModPlayer;
import su.plo.voice.client.gui.PlayerVolumeAction;

import java.util.Optional;

public final class ModPlayerVolumeAction extends PlayerVolumeAction {

    private final Minecraft minecraft = Minecraft.getInstance();

    public ModPlayerVolumeAction(@NotNull MinecraftClientLib minecraft,
                                 @NotNull PlasmoVoiceClient voiceClient, @NotNull ClientConfig config) {
        super(minecraft, voiceClient, config);
    }

    @Override
    protected Optional<MinecraftPlayer> getPlayerBySight() {
        Level level = minecraft.level;
        LocalPlayer player = minecraft.player;
        if (level == null || player == null) return Optional.empty();

        Vec3 playerPos = player.getEyePosition();
        Vec3 rotVector = player.getLookAngle();

//        points.clear();

        for (int i = 0; i < (minecraft.options.renderDistance().get() * 16); i++) {
//            points.add(playerPos);
            playerPos = playerPos.add(rotVector);
//            points.add(playerPos);
            BlockState state = level.getBlockState(new BlockPos(playerPos));
            if (!state.isAir() && state.getMaterial().isSolid()) {
                break;
            }

            AABB box = AABB.ofSize(playerPos.subtract(0, 1, 0), 1, 2, 1);

//            points.add(new Vec3(box.minX, box.minY, box.minZ));
//            points.add(new Vec3(box.maxX, box.maxY, box.maxZ));

            for (Player playerEntity : level.players()) {
                if (box.contains(playerEntity.getX(), playerEntity.getY(), playerEntity.getZ()) &&
                        !playerEntity.isInvisibleTo(player) &&
                        !player.getUUID().equals(playerEntity.getUUID())) {
                    return Optional.of(new ModPlayer(playerEntity));
                }
            }
        }


        return Optional.empty();
    }

//    private List<Vec3> points = Lists.newArrayList();
//
//    @EventSubscribe
//    public void drawLine(@NotNull LevelRenderEvent event) {
//        if (points.isEmpty()) return;
//
//        GuiRender render = event.getRender();
//
//        MinecraftTesselator tesselator = render.getTesselator();
//        VertexBuilder bufferBuilder = tesselator.getBuilder();
//
//        Vec3 cameraPos = minecraft.gameRenderer.getMainCamera().getPosition();
//
//        // setup render
//        render.disableTexture();
//        render.disableCull();
//        render.enableDepthTest();
//        render.depthMask(false);
//        render.polygonOffset(-3f, -3f);
//        render.enablePolygonOffset();
//        render.turnOnLightLayer();
//        render.depthFunc(515);
//
//        render.enableBlend();
//        render.blendFuncSeparate(
//                770, // SourceFactor.SRC_ALPHA
//                771, // DestFactor.ONE_MINUS_SRC_ALPHA
//                1, // SourceFactor.ONE
//                0 // DestFactor.ZERO
//        );
//        render.setShader(VertexBuilder.Shader.POSITION_COLOR);
//        render.setShaderColor(1F, 1F, 1F, 1F);
//
//        render.lineWidth(10F);
//
//        for (int i = 0; i < points.size(); i += 2) {
//            Vec3 start = points.get(i);
//            Vec3 end = points.get(i + 1);
//
//            bufferBuilder.begin(VertexBuilder.Mode.DEBUG_LINES, VertexBuilder.Format.POSITION_COLOR);
//            bufferBuilder.vertex(
//                    start.x - cameraPos.x,
//                    start.y - cameraPos.y,
//                    start.z - cameraPos.z
//            );
//            if (i % 4 == 0) {
//                bufferBuilder.color(192, 0, 0, 255);
//            } else {
//                bufferBuilder.color(0, 192, 0, 255);
//            }
//            bufferBuilder.endVertex();
//
//
//            bufferBuilder.vertex(
//                    end.x - cameraPos.x,
//                    end.y - cameraPos.y,
//                    end.z - cameraPos.z
//            );
//            if (i % 4 == 0) {
//                bufferBuilder.color(192, 0, 0, 255);
//            } else {
//                bufferBuilder.color(0, 192, 0, 255);
//            }
//            bufferBuilder.endVertex();
//
//            tesselator.end();
//        }
//
//        // cleanup render
//        render.polygonOffset(0f, 0f);
//        render.disablePolygonOffset();
//        render.disableBlend();
//        render.defaultBlendFunc();
//        render.disableDepthTest();
//        render.enableCull();
//        render.enableTexture();
//        render.depthMask(true);
//        render.turnOffLightLayer();
//    }
}
