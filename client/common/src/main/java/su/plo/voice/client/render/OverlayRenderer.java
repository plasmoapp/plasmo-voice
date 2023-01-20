package su.plo.voice.client.render;

import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.chat.MinecraftTextComponent;
import su.plo.lib.api.client.MinecraftClientLib;
import su.plo.lib.api.client.event.render.HudRenderEvent;
import su.plo.lib.api.client.gui.GuiRender;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.audio.line.ClientSourceLine;
import su.plo.voice.api.client.audio.source.ClientAudioSource;
import su.plo.voice.api.client.connection.ServerConnection;
import su.plo.voice.api.event.EventSubscribe;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.client.config.overlay.OverlayPosition;
import su.plo.voice.client.config.overlay.OverlaySourceState;
import su.plo.voice.proto.data.audio.source.DirectSourceInfo;
import su.plo.voice.proto.data.audio.source.SourceInfo;
import su.plo.voice.proto.data.player.MinecraftGameProfile;

import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public final class OverlayRenderer {

    private static final int ENTRY_HEIGHT = 16;

    private final MinecraftClientLib minecraft;
    private final PlasmoVoiceClient voiceClient;
    private final ClientConfig config;

    @EventSubscribe
    public void onHudRender(@NotNull HudRenderEvent event) {
        if (!voiceClient.getServerInfo().isPresent() ||
                !voiceClient.getUdpClientManager().getClient().isPresent() ||
                !minecraft.getClientPlayer().isPresent() ||
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

                renderEntry(event.getRender(), sourceLine, position, index++, sourceInfo, activated);
            }
        }
    }

    private void renderEntry(@NotNull GuiRender render,
                             @NotNull ClientSourceLine sourceLine,
                             @NotNull OverlayPosition position,
                             int index,
                             @NotNull SourceInfo sourceInfo,
                             boolean activated) {
        if (!minecraft.getWorld().isPresent()) return;

        ServerConnection connection = voiceClient.getServerConnection()
                .orElseThrow(() -> new IllegalStateException("Not connected"));

        // todo: entity renderer?
        String sourceName = getSourceSenderName(sourceInfo, sourceLine);
        MinecraftTextComponent text = MinecraftTextComponent.translatable(sourceName);

        int textWidth = minecraft.getFont().width(text) + 8;
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

        int backgroundColor = minecraft.getOptions().getBackgroundColor(Integer.MIN_VALUE);

        // render helm
        render.setShaderTexture(0, loadSkin(connection, sourceInfo, sourceName));
        render.setShaderColor(1F, 1F, 1F, 1F);

        render.blit(x, y, 16, 16, 8F, 8F, 8, 8, 64, 64);
        render.enableBlend();
        render.blit(x, y, 16, 16, 40F, 8F, 8, 8, 64, 64);
        render.disableBlend();

        if (position.isRight()) {
            x -= textWidth + 1;
        } else {
            x += 16 + 1;
        }
        render.fill(x, y, x + textWidth, y + ENTRY_HEIGHT, backgroundColor);
        render.drawString(text, x + 4, y + 4, 0xFFFFFF, false, true, 0, 15728880);

        if (activated) {
            if (position.isRight()) {
                x -= 16 + 1;
            } else {
                x += textWidth + 1;
            }
            render.fill(x, y, x + 16, y + ENTRY_HEIGHT, backgroundColor);

            render.enableTexture();
            render.setShaderTexture(0, sourceLine.getIcon());
            render.setShaderColor(1F, 1F, 1F, 1F);

            render.blit(x, y, 0, 0F, 0F, 16, 16, 16, 16);
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

        return minecraft.getConnection().get()
                .getPlayerInfo(sourceInfo.getId())
                .map((playerInfo) -> playerInfo.getGameProfile().getName())
                .orElse(sourceLine.getTranslation());
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

    private String loadSkin(@NotNull ServerConnection connection, @NotNull SourceInfo sourceInfo, @NotNull String sourceName) {
        UUID sourceId = getSourceSenderId(sourceInfo);

        return minecraft.getConnection().get()
                .getPlayerInfo(sourceId)
                .map((player) -> loadSkin(player.getGameProfile().getId(), player.getGameProfile().getName()))
                .orElseGet(() -> {
                    if (sourceInfo instanceof DirectSourceInfo) {
                        DirectSourceInfo directSourceInfo = (DirectSourceInfo) sourceInfo;

                        if (directSourceInfo.getSender() != null) {
                            return loadSkin(directSourceInfo.getSender());
                        }
                    }

                    return minecraft.getPlayerSkins().getDefaultSkin(sourceId);
                });
    }

    private String loadSkin(@NotNull MinecraftGameProfile gameProfile) {
        minecraft.getPlayerSkins().loadSkin(gameProfile);
        return minecraft.getPlayerSkins().getSkin(gameProfile.getId(), gameProfile.getName());
    }

    private String loadSkin(@NotNull UUID playerId, @NotNull String playerName) {
        minecraft.getPlayerSkins().loadSkin(
                playerId,
                playerName,
                null
        );

        return minecraft.getPlayerSkins().getSkin(playerId, playerName);
    }

    private int calcPositionX(int x) {
        if (x < 0) {
            return minecraft.getWindow().getGuiScaledWidth() + x;
        } else {
            return x;
        }
    }

    private int calcPositionY(int y) {
        if (y < 0) {
            return minecraft.getWindow().getGuiScaledHeight() + y;
        } else {
            return y;
        }
    }
}
