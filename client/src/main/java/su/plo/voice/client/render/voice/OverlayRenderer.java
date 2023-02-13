package su.plo.voice.client.render.voice;

import com.google.common.collect.Maps;
import gg.essential.universal.UGraphics;
import gg.essential.universal.UMatrixStack;
import gg.essential.universal.UMinecraft;
import gg.essential.universal.UResolution;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.chat.MinecraftTextComponent;
import su.plo.lib.mod.client.render.RenderUtil;
import su.plo.lib.mod.client.render.texture.ModPlayerSkins;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.audio.line.ClientSourceLine;
import su.plo.voice.api.client.audio.source.ClientAudioSource;
import su.plo.voice.api.client.connection.ServerConnection;
import su.plo.voice.api.event.EventSubscribe;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.client.config.overlay.OverlayPosition;
import su.plo.voice.client.config.overlay.OverlaySourceState;
import su.plo.voice.client.event.render.HudRenderEvent;
import su.plo.voice.proto.data.audio.source.DirectSourceInfo;
import su.plo.voice.proto.data.audio.source.SourceInfo;
import su.plo.voice.proto.data.player.MinecraftGameProfile;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public final class OverlayRenderer {

    private static final int ENTRY_HEIGHT = 16;

    private final PlasmoVoiceClient voiceClient;
    private final ClientConfig config;

    @EventSubscribe
    public void onHudRender(@NotNull HudRenderEvent event) {
        if (!voiceClient.getServerInfo().isPresent() ||
                !voiceClient.getUdpClientManager().getClient().isPresent() ||
                UMinecraft.getPlayer() == null ||
                !config.getOverlay().getOverlayEnabled().value()
        ) return;

        OverlayPosition position = config.getOverlay().getOverlayPosition().value();

        int index = 0;
        for (ClientSourceLine sourceLine : voiceClient.getSourceLineManager().getLines()) {
            OverlaySourceState sourceState = config.getOverlay().getSourceStates().getState(sourceLine).value();
            if (sourceState == OverlaySourceState.OFF || sourceState == OverlaySourceState.NEVER) continue;

            Map<SourceInfo, Boolean> toRender = Maps.newHashMap();
            if (sourceLine.hasPlayers() && sourceState == OverlaySourceState.ALWAYS) {
                // todo: source line players
//                for (UUID playerId : sourceLine.getPlayers()) {
//                    toRender.put(playerId, false);
//                }
            }

            for (ClientAudioSource<?> source : voiceClient.getSourceManager().getSourcesByLineId(sourceLine.getId())) {
                boolean isActivated = source.isActivated();

                if (isActivated || (sourceLine.hasPlayers() && sourceState == OverlaySourceState.ALWAYS)) {
                    toRender.put(source.getInfo(), isActivated);
                }
            }

            for (Map.Entry<SourceInfo, Boolean> entry : toRender.entrySet()) {
                SourceInfo sourceInfo = entry.getKey();
                boolean activated = entry.getValue();

                renderEntry(event.getStack(), sourceLine, position, index++, sourceInfo, activated);
            }
        }
    }

    private void renderEntry(@NotNull UMatrixStack stack,
                             @NotNull ClientSourceLine sourceLine,
                             @NotNull OverlayPosition position,
                             int index,
                             @NotNull SourceInfo sourceInfo,
                             boolean activated) {
        if (UMinecraft.getWorld() == null) return;

        ServerConnection connection = voiceClient.getServerConnection()
                .orElseThrow(() -> new IllegalStateException("Not connected"));

        // todo: entity renderer?
        String sourceName = getSourceSenderName(sourceInfo, sourceLine);
        MinecraftTextComponent text = MinecraftTextComponent.translatable(sourceName);

        int textWidth = RenderUtil.getTextWidth(text) + 8;
        int x = calcPositionX(position.getX());
        int y = calcPositionY(position.getY());

        if (position.isRight()) {
            x -= 16;
        }

        if (position.isBottom()) {
            y -= (ENTRY_HEIGHT + 1) * (index + 1);
        } else {
            y += (ENTRY_HEIGHT + 1) * index;
        }

//        int backgroundColor = minecraft.getOptions().getBackgroundColor(Integer.MIN_VALUE);
        int backgroundColor = (int) (0.25F * 255.0F) << 24;

        // render helm
        UGraphics.bindTexture(0, loadSkin(connection, sourceInfo, sourceName));
        UGraphics.color4f(1F, 1F, 1F, 1F);

        RenderUtil.blit(stack, x, y, 16, 16, 8F, 8F, 8, 8, 64, 64);
        UGraphics.enableBlend();
        RenderUtil.blit(stack, x, y, 16, 16, 40F, 8F, 8, 8, 64, 64);
        UGraphics.disableBlend();

        if (position.isRight()) {
            x -= textWidth + 1;
        } else {
            x += 16 + 1;
        }
        RenderUtil.fill(stack, x, y, x + textWidth, y + ENTRY_HEIGHT, backgroundColor);
        RenderUtil.drawString(stack, text, x + 4, y + 4, 0xFFFFFF, false);

        if (activated) {
            if (position.isRight()) {
                x -= 16 + 1;
            } else {
                x += textWidth + 1;
            }
            RenderUtil.fill(stack, x, y, x + 16, y + ENTRY_HEIGHT, backgroundColor);

            UGraphics.bindTexture(0, new ResourceLocation(sourceLine.getIcon()));
            UGraphics.color4f(1F, 1F, 1F, 1F);

            RenderUtil.blit(stack, x, y, 0, 0F, 0F, 16, 16, 16, 16);
        }
    }

    private String getSourceSenderName(@NotNull SourceInfo sourceInfo,
                                       @NotNull ClientSourceLine sourceLine) {
        if (sourceInfo instanceof DirectSourceInfo) {
            DirectSourceInfo directSourceInfo = (DirectSourceInfo) sourceInfo;

            if (directSourceInfo.getSender() != null) {
                return directSourceInfo.getSender().getName();
            }
        }

        PlayerInfo playerInfo = UMinecraft.getNetHandler().getPlayerInfo(sourceInfo.getId());
        if (playerInfo == null) return sourceLine.getTranslation();

        return playerInfo.getProfile().getName();
    }

    private UUID getSourceSenderId(@NotNull SourceInfo sourceInfo) {
        UUID sourceId = sourceInfo.getId();
        if (sourceInfo instanceof DirectSourceInfo) {
            DirectSourceInfo directSourceInfo = (DirectSourceInfo) sourceInfo;

            if (directSourceInfo.getSender() != null) {
                sourceId = directSourceInfo.getSender().getId();
            }
        }

        return sourceId;
    }

    private ResourceLocation loadSkin(@NotNull ServerConnection connection, @NotNull SourceInfo sourceInfo, @NotNull String sourceName) {
        UUID sourceId = getSourceSenderId(sourceInfo);

        return Optional.ofNullable(UMinecraft.getNetHandler().getPlayerInfo(sourceId))
                .map((player) -> loadSkin(player.getProfile().getId(), player.getProfile().getName()))
                .orElseGet(() -> {
                    if (sourceInfo instanceof DirectSourceInfo) {
                        DirectSourceInfo directSourceInfo = (DirectSourceInfo) sourceInfo;

                        if (directSourceInfo.getSender() != null) {
                            return loadSkin(directSourceInfo.getSender());
                        }
                    }

                    return ModPlayerSkins.getDefaultSkin(sourceId);
                });
    }

    private ResourceLocation loadSkin(@NotNull MinecraftGameProfile gameProfile) {
        ModPlayerSkins.loadSkin(gameProfile);
        return ModPlayerSkins.getSkin(gameProfile.getId(), gameProfile.getName());
    }

    private ResourceLocation loadSkin(@NotNull UUID playerId, @NotNull String playerName) {
        ModPlayerSkins.loadSkin(
                playerId,
                playerName,
                null
        );

        return ModPlayerSkins.getSkin(playerId, playerName);
    }

    private int calcPositionX(int x) {
        if (x < 0) {
            return UResolution.getScaledWidth() + x;
        } else {
            return x;
        }
    }

    private int calcPositionY(int y) {
        if (y < 0) {
            return UResolution.getScaledHeight() + y;
        } else {
            return y;
        }
    }
}
