package su.plo.voice.paper.connection;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;
import org.apache.logging.log4j.LogManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRegisterChannelEvent;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.proto.packets.tcp.PacketTcpCodec;
import su.plo.voice.server.BaseVoiceServer;
import su.plo.voice.server.connection.BaseServerChannelHandler;
import su.plo.voice.server.connection.PlayerChannelHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public final class PaperServerChannelHandler extends BaseServerChannelHandler implements PluginMessageListener, Listener {

    private final Map<UUID, List<String>> channelsUpdates = Maps.newConcurrentMap();
    private final Map<UUID, ScheduledFuture<?>> channelsFutures = Maps.newConcurrentMap();

    public PaperServerChannelHandler(@NotNull BaseVoiceServer voiceServer,
                                     @NotNull ScheduledExecutorService executor) {
        super(voiceServer, executor);
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channelName, @NotNull Player player, @NotNull byte[] message) {
        try {
            PacketTcpCodec.decode(ByteStreams.newDataInput(message))
                    .ifPresent(packet -> {
                        LogManager.getLogger().info("Channel packet received {}", packet);

                        VoicePlayer voicePlayer = voiceServer.getPlayerManager().wrap(player);

                        PlayerChannelHandler channel = channels.computeIfAbsent(
                                player.getUniqueId(),
                                (playerId) -> new PlayerChannelHandler(voiceServer, voicePlayer)
                        );

                        channel.handlePacket(packet);
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onPlayerRegisterChannel(@NotNull PlayerRegisterChannelEvent event) {
        Player player = event.getPlayer();
        String channel = event.getChannel();

        List<String> updates = channelsUpdates.computeIfAbsent(
                player.getUniqueId(),
                (playerId) -> Lists.newArrayList()
        );
        if (updates.contains(channel)) return;
        updates.add(channel);

        ScheduledFuture<?> future = channelsFutures.get(player.getUniqueId());
        if (future != null) future.cancel(false);

        channelsFutures.put(player.getUniqueId(), executor.schedule(() -> {
            List<String> channels = channelsUpdates.remove(player.getUniqueId());
            channelsFutures.remove(player.getUniqueId());

            if (channels == null) return;

            handleRegisterChannels(channels, voiceServer.getPlayerManager().wrap(player));
        }, 500L, TimeUnit.MILLISECONDS));
    }
}
