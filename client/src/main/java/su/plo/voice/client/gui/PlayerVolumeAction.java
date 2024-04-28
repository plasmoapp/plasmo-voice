package su.plo.voice.client.gui;

import su.plo.lib.mod.extensions.AABBKt;
import gg.essential.universal.UMinecraft;
import gg.essential.universal.UScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import su.plo.config.entry.DoubleConfigEntry;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.config.hotkey.Hotkey;
import su.plo.voice.api.client.connection.ServerConnection;
import su.plo.voice.api.event.EventSubscribe;
import su.plo.voice.client.config.VoiceClientConfig;
import su.plo.voice.client.event.key.MouseScrollEvent;

import java.util.Optional;

import static su.plo.lib.mod.extensions.EntityKt.eyePosition;
import static su.plo.voice.client.extension.OptionsKt.renderDistanceValue;

public final class PlayerVolumeAction {

    private final PlasmoVoiceClient voiceClient;
    private final VoiceClientConfig config;

    private Player focusedPlayer;
    private long lastScroll;

    public PlayerVolumeAction(@NotNull PlasmoVoiceClient voiceClient,
                              @NotNull VoiceClientConfig config) {
        this.voiceClient = voiceClient;
        this.config = config;

        voiceClient.getHotkeys().getHotkey("key.plasmovoice.general.action")
                .ifPresent((key) -> key.addPressListener(this::onButton));
    }

    public boolean isShown(@NotNull Player player) {
        return focusedPlayer != null &&
                focusedPlayer.getUUID().equals(player.getUUID()) &&
                lastScroll != 0L &&
                System.currentTimeMillis() - lastScroll < 1_000L;
    }

    @EventSubscribe
    public void onScroll(@NotNull MouseScrollEvent event) {

        if (focusedPlayer != null && UScreen.getCurrentScreen() == null) {
            this.lastScroll = System.currentTimeMillis();

            DoubleConfigEntry volume = config.getVoice().getVolumes().getVolume("source_" + focusedPlayer.getUUID());

            double value = volume.value() + (event.getVertical() > 0 ? 0.05D : -0.05D);
            volume.set((Math.round((value * volume.getMax() * 100D) / 5) * 5) / (volume.getMax() * 100D));

            event.setCancelled(true);
        }
    }

    private void onButton(@NotNull Hotkey.Action action) {
        if (!voiceClient.getServerConnection().isPresent()) return;

        if (action == Hotkey.Action.DOWN) {
            ServerConnection serverConnection = voiceClient.getServerConnection().get();

            getPlayerBySight()
                    .filter((player) -> serverConnection.getPlayerById(player.getUUID()).isPresent())
                    .ifPresent((player) -> this.focusedPlayer = player);
        } else if (action == Hotkey.Action.UP) {
            this.focusedPlayer = null;
            this.lastScroll = 0L;
        }
    }

    private Optional<Player> getPlayerBySight() {
        ClientLevel level = UMinecraft.getWorld();
        LocalPlayer player = UMinecraft.getPlayer();
        if (level == null || player == null) return Optional.empty();

        Vec3 playerPos = eyePosition(player);
        Vec3 rotVector = player.getLookAngle();

//        points.clear();

        for (int i = 0; i < (renderDistanceValue(UMinecraft.getSettings()) * 16); i++) {
//            points.add(playerPos);
            playerPos = playerPos.add(rotVector);
//            points.add(playerPos);
            BlockPos blockPos = new BlockPos(
                    (int) Math.floor(playerPos.x),
                    (int) Math.floor(playerPos.y),
                    (int) Math.floor(playerPos.z)
            );
            BlockState state = level.getBlockState(blockPos);
            //#if MC>=12000
            //$$ boolean isSolid = state.isSolidRender(level, blockPos);
            //#else
            boolean isSolid = state.getMaterial().isSolid();
            //#endif
            if (!state.isAir() && isSolid) {
                break;
            }

            AABB box = AABBKt.ofSize(playerPos.subtract(0, 1, 0), 1, 2, 1);

//            points.add(new Vec3(box.minX, box.minY, box.minZ));
//            points.add(new Vec3(box.maxX, box.maxY, box.maxZ));

            for (Player playerEntity : level.players()) {
                if (box.contains(playerEntity.getX(), playerEntity.getY(), playerEntity.getZ()) &&
                        !playerEntity.isInvisibleTo(player) &&
                        !player.getUUID().equals(playerEntity.getUUID())) {
                    return Optional.of(playerEntity);
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
