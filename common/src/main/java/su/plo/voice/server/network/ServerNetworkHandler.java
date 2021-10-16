package su.plo.voice.server.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import su.plo.voice.common.entities.MutedEntity;
import su.plo.voice.common.packets.Packet;
import su.plo.voice.common.packets.tcp.*;
import su.plo.voice.server.PlayerManager;
import su.plo.voice.server.VoiceServer;
import su.plo.voice.server.config.ServerConfig;
import su.plo.voice.server.config.ServerMuted;
import su.plo.voice.server.socket.SocketClientUDP;
import su.plo.voice.server.socket.SocketServerUDP;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class ServerNetworkHandler {
    protected ScheduledExecutorService scheduler;
    private static ExecutorService executor;
    public static HashMap<UUID, UUID> playerToken = new HashMap<>();

    public ServerNetworkHandler() {
    }

    public boolean isVanillaPlayer(ServerPlayer player) {
        return true;
    }

    public void start() {
        this.scheduler = Executors.newScheduledThreadPool(1);
        executor = Executors.newSingleThreadExecutor();

        scheduler.scheduleAtFixedRate(() -> {
            VoiceServer.getMuted().forEach((uuid, muted) -> {
                if (muted.getTo() > 0 && muted.getTo() < System.currentTimeMillis()) {
                    VoiceServer.getMuted().remove(uuid);
                    VoiceServer.saveData(true);
                    ServerPlayer player = PlayerManager.getByUUID(uuid);
                    if (player != null) {
                        sendToClients(new ClientUnmutedPacket(uuid), null);
                    }
                }
            });
        }, 0L, 5L, TimeUnit.SECONDS);
    }

    public void close() {
        scheduler.shutdown();
        executor.shutdown();
    }

    public void handleRegisterChannels(List<ResourceLocation> channels, ServerPlayer player) {
        if (!playerToken.containsKey(player.getUUID()) && channels.contains(VoiceServer.PLASMO_VOICE)
                && !SocketServerUDP.clients.containsKey(player.getUUID())) {
            ServerNetworkHandler.reconnectClient(player);
        }
    }

    public void handleJoin(ServerPlayer player) {
        if (PlayerManager.isOp(player)
                && !SocketServerUDP.started) {
            player.sendMessage(new TextComponent(VoiceServer.getInstance().getPrefix() +
                            String.format("Voice chat is installed but doesn't work. Check if port %d UDP is open.",
                                    VoiceServer.getServerConfig().getPort())),
                    VoiceServer.NIL_UUID);
        }
    }

    public void handleQuit(ServerPlayer player) {
        playerToken.remove(player.getUUID());
        disconnectClient(player.getUUID());
    }

    public void handle(ClientConnectPacket packet, ServerPlayer player) throws IOException {
        ServerConfig config = VoiceServer.getServerConfig();

        String version = packet.getVersion();
        int ver = VoiceServer.calculateVersion(version);

        if (ver > VoiceServer.version) {
            player.sendMessage(
                    new TranslatableComponent("message.plasmo_voice.version_not_supported", VoiceServer.rawVersion),
                    VoiceServer.NIL_UUID
            );
            return;
        } else if (ver < VoiceServer.minVersion) {
            player.sendMessage(
                    new TranslatableComponent("message.plasmo_voice.min_version", VoiceServer.rawMinVersion),
                    VoiceServer.NIL_UUID
            );
            return;
        } else if (ver < VoiceServer.version) {
            TextComponent link = new TextComponent("https://www.curseforge.com/minecraft/mc-mods/plasmo-voice-client/files");
            link.setStyle(link.getStyle().withClickEvent(
                    new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.curseforge.com/minecraft/mc-mods/plasmo-voice-client/files")
            ));

            player.sendMessage(
                    new TranslatableComponent(
                            "message.plasmo_voice.new_version_available",
                            VoiceServer.rawVersion,
                            link
                    ),
                    VoiceServer.NIL_UUID
            );
        }

        sendTo(new ConfigPacket(config.getSampleRate(),
                        new ArrayList<>(config.getDistances()),
                        config.getDefaultDistance(),
                        config.getMaxPriorityDistance(),
                        config.isDisableVoiceActivation() ||
                                !VoiceServer.getPlayerManager().hasPermission(player.getUUID(), "voice.activation"),
                        config.getFadeDivisor(),
                        config.getPriorityFadeDivisor()),
                player);

        List<UUID> clients = new ArrayList<>();
        SocketServerUDP.clients.forEach((uuid, c) -> clients.add(uuid));

        List<MutedEntity> muted = new ArrayList<>();
        VoiceServer.getMuted().forEach((uuid, m) -> muted.add(new MutedEntity(m.getUuid(), m.getTo())));

        ServerMuted serverPlayerMuted = VoiceServer.getMuted().get(UUID.randomUUID());
        MutedEntity playerMuted = null;
        if (serverPlayerMuted != null) {
            playerMuted = new MutedEntity(serverPlayerMuted.getUuid(), serverPlayerMuted.getTo());
        }
        if (!VoiceServer.getPlayerManager().hasPermission(player.getUUID(), "voice.speak")) {
            playerMuted = new MutedEntity(player.getUUID(), 0L);
        }

        sendTo(new ClientsListPacket(clients, muted), player);

        sendToClients(new ClientConnectedPacket(player.getUUID(), playerMuted), player.getUUID());

        if (!VoiceServer.getInstance().getConfig().getBoolean("disable_logs")) {
            VoiceServer.LOGGER.info(String.format("New client: %s v%s", player.getName(), version));
        }
    }

    public static void reconnectClient(ServerPlayer player) {
        UUID token = UUID.randomUUID();
        playerToken.put(player.getUUID(), token);

        executor.submit(() -> {
            try {
                sendTo(new ServerConnectPacket(token.toString(),
                                VoiceServer.getServerConfig().getProxyIp() != null && !VoiceServer.getServerConfig().getProxyIp().isEmpty()
                                        ? VoiceServer.getServerConfig().getProxyIp()
                                        : VoiceServer.getServerConfig().getIp(),
                                VoiceServer.getServerConfig().getProxyPort() != 0
                                        ? VoiceServer.getServerConfig().getProxyPort()
                                        : VoiceServer.getServerConfig().getPort(),
                                VoiceServer.getPlayerManager().hasPermission(player.getUUID(), "voice.priority")),
                        player);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static void disconnectClient(UUID uuid) {
        SocketClientUDP clientUDP = SocketServerUDP.clients.get(uuid);

        try {
            if (clientUDP != null) {
                clientUDP.close();
                sendToClients(new ClientDisconnectedPacket(uuid), null);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void sendTo(Packet packet, ServerPlayer player) throws IOException {
        player.connection.send(new ClientboundCustomPayloadPacket(VoiceServer.PLASMO_VOICE,
                new FriendlyByteBuf(Unpooled.wrappedBuffer(PacketTCP.write(packet)))));
    }

    public static void sendToClients(Packet packet, UUID except) {
        executor.submit(() -> {
            try {
                byte[] pkt = PacketTCP.write(packet);
                Enumeration<UUID> it = SocketServerUDP.clients.keys();
                while (it.hasMoreElements()) {
                    UUID uuid = it.nextElement();
                    if (!uuid.equals(except)) {
                        ServerPlayer player = PlayerManager.getByUUID(uuid);

                        player.connection.send(new ClientboundCustomPayloadPacket(
                                VoiceServer.PLASMO_VOICE,
                                new FriendlyByteBuf(Unpooled.wrappedBuffer(pkt))));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
