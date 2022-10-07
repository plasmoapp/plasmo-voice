package su.plo.voice.client.render;

import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.chat.TextComponent;
import su.plo.lib.client.MinecraftClientLib;
import su.plo.lib.client.event.render.HudRenderEvent;
import su.plo.lib.client.gui.GuiRender;
import su.plo.lib.entity.MinecraftPlayer;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.audio.line.ClientSourceLine;
import su.plo.voice.api.client.audio.source.ClientAudioSource;
import su.plo.voice.api.client.connection.ServerConnection;
import su.plo.voice.api.event.EventSubscribe;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.client.config.overlay.OverlayPosition;
import su.plo.voice.client.config.overlay.OverlaySourceState;

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

        for (ClientSourceLine sourceLine : voiceClient.getSourceLineManager().getLines()) {
            OverlaySourceState sourceState = config.getOverlay().getSourceStates().getState(sourceLine).value();
            if (sourceState == OverlaySourceState.OFF || sourceState == OverlaySourceState.NEVER) continue;

            Map<UUID, Boolean> toRender = Maps.newHashMap();
            if (sourceLine.hasPlayers() && sourceState == OverlaySourceState.ALWAYS) {
                for (UUID playerId : sourceLine.getPlayers()) {
                    toRender.put(playerId, false);
                }
            }

            for (ClientAudioSource<?> source : voiceClient.getSourceManager().getSourcesByLineId(sourceLine.getId())) {
                boolean isActivated = source.isActivated();

                if (isActivated || (sourceLine.hasPlayers() && sourceState == OverlaySourceState.ALWAYS)) {
                    toRender.put(source.getInfo().getId(), isActivated);
                }
            }

            int index = 0;
            for (Map.Entry<UUID, Boolean> entry : toRender.entrySet()) {
                UUID sourceId = entry.getKey();
                boolean activated = entry.getValue();

                renderEntry(event.getRender(), sourceLine, position, index++, sourceId, activated);
            }
        }
    }

    private void renderEntry(@NotNull GuiRender render,
                             @NotNull ClientSourceLine sourceLine,
                             @NotNull OverlayPosition position,
                             int index,
                             @NotNull UUID sourceId,
                             boolean activated) {
        if (!minecraft.getWorld().isPresent()) return;

        ServerConnection connection = voiceClient.getServerConnection()
                .orElseThrow(() -> new IllegalStateException("Not connected"));

        // todo: entity renderer?
        String sourceName = minecraft.getWorld().get()
                .getPlayerById(sourceId)
                .map(MinecraftPlayer::getName)
                .orElse(sourceLine.getTranslation());
        TextComponent text = TextComponent.translatable(sourceName);

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

        // render helm
        render.setShaderTexture(0, loadSkin(connection, sourceId, sourceName));
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
        render.fill(x, y, x + textWidth, y + ENTRY_HEIGHT, minecraft.getOptions().getBackgroundColor(Integer.MIN_VALUE));
        render.drawString(text, x + 4, y + 4, 0xFFFFFF, false);

        if (activated) {
            if (position.isRight()) {
                x -= 16 + 1;
            } else {
                x += textWidth + 1;
            }
            render.fill(x, y, x + 16, y + ENTRY_HEIGHT, minecraft.getOptions().getBackgroundColor(Integer.MIN_VALUE));

            render.enableTexture();
            render.setShaderTexture(0, sourceLine.getIcon());
            render.setShaderColor(1F, 1F, 1F, 1F);

            render.enableBlend();
            render.defaultBlendFunc();
            render.enableDepthTest();

            render.blit(x, y, 0, 0F, 0F, 16, 16, 16, 16);
        }
    }

    private String loadSkin(@NotNull ServerConnection connection, @NotNull UUID sourceId, @NotNull String sourceName) {
        return connection.getPlayerById(sourceId)
                .map((player) -> {
                    minecraft.getPlayerSkins().loadSkin(
                            player.getPlayerId(),
                            player.getPlayerNick(),
                            null
                    );
                    return minecraft.getPlayerSkins().getSkin(player.getPlayerId(), player.getPlayerNick());
                })
                .orElseGet(() -> minecraft.getPlayerSkins().getSteveSkin());
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
