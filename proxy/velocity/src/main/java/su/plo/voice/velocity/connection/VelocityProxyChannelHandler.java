package su.plo.voice.velocity.connection;

import com.google.common.collect.Maps;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.proxy.player.VoiceProxyPlayer;
import su.plo.voice.proto.packets.PacketHandler;
import su.plo.voice.proto.packets.PacketUtil;
import su.plo.voice.proto.packets.tcp.PacketTcpCodec;
import su.plo.voice.proxy.BaseVoiceProxy;
import su.plo.voice.proxy.connection.CancelForwardingException;
import su.plo.voice.proxy.connection.PlayerToServerChannelHandler;
import su.plo.voice.proxy.connection.ServerToPlayerChannelHandler;
import su.plo.voice.proxy.server.VoiceRemoteServer;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.UUID;

public class VelocityProxyChannelHandler {

    public static final ChannelIdentifier VOICE_CHANNEL = MinecraftChannelIdentifier.from(BaseVoiceProxy.CHANNEL_STRING);
    public static final ChannelIdentifier VOICE_SERVICE_CHANNEL = MinecraftChannelIdentifier.from(BaseVoiceProxy.SERVICE_CHANNEL_STRING);

    private final Map<UUID, PlayerToServerChannelHandler> playerToServerChannels = Maps.newHashMap();
    private final Map<UUID, ServerToPlayerChannelHandler> serverToPlayerChannels = Maps.newHashMap();

    private final BaseVoiceProxy voiceProxy;

    public VelocityProxyChannelHandler(@NotNull ProxyServer server,
                                       @NotNull BaseVoiceProxy voiceProxy) {
        this.voiceProxy = voiceProxy;

        server.getChannelRegistrar().register(VOICE_CHANNEL, VOICE_SERVICE_CHANNEL);
    }

    @Subscribe
    public void onPlasmoVoiceServicePacket(@NotNull PluginMessageEvent event) {
        if (!event.getResult().isAllowed()) return;
        if (!event.getIdentifier().equals(VOICE_SERVICE_CHANNEL)) return;

        if (event.getSource() instanceof ServerConnection) {
            ServerConnection connection = (ServerConnection) event.getSource();

            try {
                ByteArrayDataInput input = ByteStreams.newDataInput(event.getData());
                byte[] signature = PacketUtil.readBytes(input, 32);

                byte[] aesEncryptionKey = voiceProxy.getConfig().aesEncryptionKey();

                SecretKey key = new SecretKeySpec(
                        PacketUtil.getUUIDBytes(voiceProxy.getConfig().forwardingSecret()),
                        "HmacSHA256"
                );
                Mac mac = Mac.getInstance("HmacSHA256");
                mac.init(key);
                mac.update(aesEncryptionKey, 0, aesEncryptionKey.length);

                if (!MessageDigest.isEqual(signature, mac.doFinal())) {
                    LogManager.getLogger().warn("Received invalid AES key signature from {}", connection);
                    return;
                }

                voiceProxy.getRemoteServerManager().getServer(connection.getServerInfo().getName()).ifPresent((server) -> {
                    ((VoiceRemoteServer) server).setAesEncryptionKeySet(true);
                });
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            } catch (IOException | InvalidKeyException e) {
                e.printStackTrace();
            } finally {
                event.setResult(PluginMessageEvent.ForwardResult.handled());
            }
        }
    }

    @Subscribe
    public void onPlasmoVoicePacket(@NotNull PluginMessageEvent event) {
        if (!event.getResult().isAllowed()) return;
        if (!event.getIdentifier().equals(VOICE_CHANNEL)) return;

        try {
            PacketTcpCodec.decode(ByteStreams.newDataInput(event.getData()))
                    .ifPresent((packet) -> {
                        PacketHandler handler;
                        if (event.getSource() instanceof Player) {
                            Player player = (Player) event.getSource();
                            VoiceProxyPlayer voicePlayer = voiceProxy.getPlayerManager().wrap(player);

                            handler = playerToServerChannels.computeIfAbsent(
                                    player.getUniqueId(),
                                    (playerId) -> new PlayerToServerChannelHandler(voiceProxy, voicePlayer)
                            );
                        } else if (event.getTarget() instanceof Player) {
                            Player player = (Player) event.getTarget();
                            VoiceProxyPlayer voicePlayer = voiceProxy.getPlayerManager().wrap(player);

                            handler = serverToPlayerChannels.computeIfAbsent(
                                    player.getUniqueId(),
                                    (playerId) -> new ServerToPlayerChannelHandler(voiceProxy, voicePlayer)
                            );
                        } else return;

                        try {
                            packet.handle(handler);
                        } catch (CancelForwardingException ignored) {
                            event.setResult(PluginMessageEvent.ForwardResult.handled());
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Subscribe
    public void onPlayerQuit(@NotNull DisconnectEvent event) {
        Player player = event.getPlayer();
        playerToServerChannels.remove(player.getUniqueId());
        serverToPlayerChannels.remove(player.getUniqueId());
    }
}
