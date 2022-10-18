package su.plo.voice.client.connection;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.lib.api.client.MinecraftClientLib;
import su.plo.voice.api.client.audio.capture.AudioCapture;
import su.plo.voice.api.client.audio.capture.ClientActivationManager;
import su.plo.voice.api.client.audio.device.OutputDevice;
import su.plo.voice.api.client.audio.device.source.AlSource;
import su.plo.voice.api.client.audio.line.ClientSourceLine;
import su.plo.voice.api.client.audio.line.ClientSourceLineManager;
import su.plo.voice.api.client.audio.source.ClientSourceManager;
import su.plo.voice.api.client.connection.ServerConnection;
import su.plo.voice.api.client.connection.ServerInfo;
import su.plo.voice.api.client.event.connection.*;
import su.plo.voice.api.client.event.socket.UdpClientClosedEvent;
import su.plo.voice.api.client.event.socket.UdpClientConnectEvent;
import su.plo.voice.api.client.socket.UdpClient;
import su.plo.voice.api.encryption.Encryption;
import su.plo.voice.api.event.EventSubscribe;
import su.plo.voice.api.util.Params;
import su.plo.voice.client.BaseVoiceClient;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.client.socket.NettyUdpClient;
import su.plo.voice.proto.data.audio.source.PlayerSourceInfo;
import su.plo.voice.proto.data.encryption.EncryptionInfo;
import su.plo.voice.proto.data.player.VoicePlayerInfo;
import su.plo.voice.proto.packets.Packet;
import su.plo.voice.proto.packets.PacketHandler;
import su.plo.voice.proto.packets.tcp.clientbound.*;
import su.plo.voice.proto.packets.tcp.serverbound.PlayerInfoPacket;

import javax.crypto.Cipher;
import javax.sound.sampled.AudioFormat;
import java.net.InetSocketAddress;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public abstract class BaseServerConnection implements ServerConnection, ClientPacketTcpHandler {

    private static final Logger LOGGER = LogManager.getLogger();

    private final BaseVoiceClient voiceClient;
    private final MinecraftClientLib minecraft;
    private final ClientSourceLineManager sourceLines;
    private final ClientActivationManager activations;
    private final ClientSourceManager sources;

    @Getter
    private final PrivateKey privateKey;
    @Getter
    private final PublicKey publicKey;

    private final Map<UUID, VoicePlayerInfo> playerById = Maps.newConcurrentMap();

    private @Nullable EncryptionInfo encryptionInfo;

    public BaseServerConnection(@NotNull BaseVoiceClient voiceClient,
                                @NotNull MinecraftClientLib minecraft) throws Exception {
        this.voiceClient = voiceClient;
        this.minecraft = minecraft;
        this.sourceLines = voiceClient.getSourceLineManager();
        this.activations = voiceClient.getActivationManager();
        this.sources = voiceClient.getSourceManager();

        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair pair = generator.generateKeyPair();
        this.privateKey = pair.getPrivate();
        this.publicKey = pair.getPublic();
    }

    @Override
    public Collection<VoicePlayerInfo> getPlayers() {
        return playerById.values();
    }

    @Override
    public Optional<VoicePlayerInfo> getPlayerById(@NotNull UUID playerId) {
        return Optional.ofNullable(playerById.get(playerId));
    }

    @Override
    public Optional<VoicePlayerInfo> getClientPlayer() {
        return minecraft.getClientPlayer()
                .flatMap(player -> getPlayerById(player.getUUID()));
    }

    @Override
    public Optional<EncryptionInfo> getEncryptionInfo() {
        return Optional.ofNullable(encryptionInfo);
    }

    @Override
    public void close() {
        // cleanup server connection
        playerById.clear();

        // cleanup audio capture
        voiceClient.getAudioCapture().stop();

        // cleanup sources
        voiceClient.getSourceManager().clear();

        // cleanup source lines
        voiceClient.getSourceLineManager().clear();

        // cleanup activations
        voiceClient.getActivationManager().clear();

        // cleanup devices
        voiceClient.getDeviceManager().clear(null);
    }

    @EventSubscribe
    public void onUdpClosed(@NotNull UdpClientClosedEvent event) {
        close();
    }

    public void handle(Packet<PacketHandler> packet) {
        TcpClientPacketReceivedEvent event = new TcpClientPacketReceivedEvent(this, packet);
        voiceClient.getEventBus().call(event);
        if (event.isCancelled()) return;

        LogManager.getLogger().info("Channel packet received {}", packet);
        packet.handle(this);
    }

    @Override
    public void handle(@NotNull ConnectionPacket packet) {
        voiceClient.getUdpClientManager().removeClient(UdpClientClosedEvent.Reason.RECONNECT);

        UdpClient client = new NettyUdpClient(voiceClient, packet.getSecret());

        UdpClientConnectEvent connectEvent = new UdpClientConnectEvent(client, packet);
        voiceClient.getEventBus().call(connectEvent);
        if (connectEvent.isCancelled()) return;

        client = connectEvent.getClient();

        voiceClient.getUdpClientManager().setClient(client);

        String ip = packet.getIp();
        if (ip.equals("0.0.0.0")) ip = voiceClient.getServerIp();

        try {
            client.connect(ip, packet.getPort());
        } catch (Exception e) {
            LOGGER.error("Failed to connect to the UDP server", e);
        }
    }

    @Override
    public void handle(@NotNull ConfigPacket packet) {
        Optional<UdpClient> client = voiceClient.getUdpClientManager().getClient();
        if (!client.isPresent()) {
            LOGGER.warn("Config packet is received before UDP is connected");
            return;
        }

        Optional<InetSocketAddress> remoteAddress = client.get().getRemoteAddress();
        if (!remoteAddress.isPresent()) {
            LOGGER.warn("Config packet is received before UDP is connected");
            return;
        }

        // initialize encryption
        this.encryptionInfo = packet.getEncryption();
        Encryption encryption = null;
        if (encryptionInfo != null) {
            try {
                Cipher decryptCipher = Cipher.getInstance("RSA");
                decryptCipher.init(Cipher.DECRYPT_MODE, privateKey);
                byte[] encryptionData = decryptCipher.doFinal(encryptionInfo.getData());

                encryption = voiceClient.getEncryptionManager().create(
                        encryptionInfo.getAlgorithm(),
                        encryptionData
                );
            } catch (Exception e) {
                LOGGER.error("Failed to initialize encryption with name {}", encryptionInfo.getAlgorithm(), e);
                return;
            }
        }

        ServerInfo serverInfo = new VoiceServerInfo(
                packet.getServerId(),
                client.get().getSecret(),
                remoteAddress.get(),
                encryption,
                packet
        );

        voiceClient.setServerInfo(serverInfo);

        Optional<ClientConfig.Server> configServer = voiceClient.getConfig().getServers().getById(serverInfo.getServerId());
        if (!configServer.isPresent()) { // put config server if it doesn't exist
            voiceClient.getConfig().getServers().put(
                    serverInfo.getServerId(),
                    new ClientConfig.Server()
            );
        }

        // register source lines
        voiceClient.getSourceLineManager()
                .register(serverInfo.getVoiceInfo().getSourceLines());

        // register activations
        voiceClient.getActivationManager()
                .register(serverInfo.getVoiceInfo().getActivations());

        // initialize capture
        AudioCapture audioCapture = voiceClient.getAudioCapture();
        audioCapture.start();
        audioCapture.initialize(serverInfo);

        // clear & initialize primary output device
        AudioFormat format = new AudioFormat(
                (float) serverInfo.getVoiceInfo().getCapture().getSampleRate(),
                16,
                1,
                true,
                false
        );

        try {
            OutputDevice<AlSource> outputDevice = voiceClient.getDeviceManager().openOutputDevice(format, Params.EMPTY);
            voiceClient.getDeviceManager().add(outputDevice);
        } catch (Exception e) {
            LOGGER.error("Failed to open primary OpenAL output device", e);
            return;
        }

        ServerInfoInitializedEvent event = new ServerInfoInitializedEvent(serverInfo, packet);
        voiceClient.getEventBus().call(event);
    }

    @Override
    public void handle(@NotNull PlayerInfoRequestPacket packet) {
        sendPacket(new PlayerInfoPacket(
                voiceClient.getVersion(),
                publicKey.getEncoded(),
                voiceClient.getConfig().getVoice().getDisabled().value(),
                voiceClient.getConfig().getVoice().getMicrophoneDisabled().value()
        ));
    }

    @Override
    public void handle(@NotNull ConfigPlayerInfoPacket packet) {
        voiceClient.getServerInfo()
                .ifPresent(serverInfo ->
                        ((VoiceServerInfo.VoiceServerPlayerInfo) serverInfo.getPlayerInfo())
                                .update(packet.getPermissions())
                );
    }

    @Override
    public void handle(@NotNull PlayerListPacket packet) {
        packet.getPlayers()
                .forEach((player) -> playerById.put(player.getPlayerId(), player));
    }

    @Override
    public void handle(@NotNull PlayerInfoUpdatePacket packet) {
        if (playerById.put(packet.getPlayerInfo().getPlayerId(), packet.getPlayerInfo()) == null) {
            voiceClient.getEventBus().call(new VoicePlayerConnectedEvent(packet.getPlayerInfo()));
        } else {
            voiceClient.getEventBus().call(new VoicePlayerUpdateEvent(packet.getPlayerInfo()));
        }
    }

    @Override
    public void handle(@NotNull PlayerDisconnectPacket packet) {
        if (minecraft.getClientPlayer().map(player -> player.getUUID().equals(player.getUUID())).orElse(false)) {
            voiceClient.getUdpClientManager().removeClient(UdpClientClosedEvent.Reason.DISCONNECT);
            return;
        }

        playerById.remove(packet.getPlayerId());
        voiceClient.getEventBus().call(new VoicePlayerDisconnectedEvent(packet.getPlayerId()));
    }

    @Override
    public void handle(@NotNull SourceAudioEndPacket packet) {
        sources.getSourceById(packet.getSourceId())
                .ifPresent(source -> source.process(packet));
    }

    @Override
    public void handle(@NotNull SourceInfoPacket packet) {
        if (packet.getSourceInfo() instanceof PlayerSourceInfo) {
            PlayerSourceInfo sourceInfo = (PlayerSourceInfo) packet.getSourceInfo();
            playerById.put(sourceInfo.getPlayerInfo().getPlayerId(), sourceInfo.getPlayerInfo());
        }

        sources.update(packet.getSourceInfo());
    }

    @Override
    public void handle(@NotNull SourceLineRegisterPacket packet) {
        sourceLines.register(packet.getSourceLine());
    }

    @Override
    public void handle(@NotNull SourceLineUnregisterPacket packet) {
        sourceLines.unregister(packet.getLineId());
    }

    @Override
    public void handle(@NotNull SourceLinePlayerAddPacket packet) {
        sourceLines.getLineById(packet.getLineId())
                .ifPresent((line) -> line.addPlayer(packet.getPlayerId()));
    }

    @Override
    public void handle(@NotNull SourceLinePlayerRemovePacket packet) {
        sourceLines.getLineById(packet.getLineId())
                .ifPresent((line) -> line.removePlayer(packet.getPlayerId()));
    }

    @Override
    public void handle(@NotNull SourceLinePlayersClearPacket packet) {
        sourceLines.getLineById(packet.getLineId())
                .ifPresent(ClientSourceLine::clearPlayers);
    }

    @Override
    public void handle(@NotNull ActivationRegisterPacket packet) {
        Optional<ServerInfo> serverInfo = voiceClient.getServerInfo();
        if (!serverInfo.isPresent()) return;

        activations.register(Lists.newArrayList(packet.getActivation()));
    }

    @Override
    public void handle(@NotNull ActivationUnregisterPacket packet) {
        activations.unregister(packet.getActivationId());
    }

    @Override
    public void handle(@NotNull DistanceVisualizePacket packet) {
        voiceClient.getDistanceVisualizer().render(packet.getRadius(), packet.getHexColor());
    }
}
