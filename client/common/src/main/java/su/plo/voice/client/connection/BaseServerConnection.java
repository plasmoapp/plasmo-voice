package su.plo.voice.client.connection;

import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.client.audio.capture.AudioCapture;
import su.plo.voice.api.client.audio.capture.ClientActivationManager;
import su.plo.voice.api.client.audio.device.OutputDevice;
import su.plo.voice.api.client.audio.device.source.AlSource;
import su.plo.voice.api.client.audio.line.ClientSourceLineManager;
import su.plo.voice.api.client.connection.ServerConnection;
import su.plo.voice.api.client.connection.ServerInfo;
import su.plo.voice.api.client.event.connection.ServerInfoUpdateEvent;
import su.plo.voice.api.client.event.socket.UdpClientClosedEvent;
import su.plo.voice.api.client.event.socket.UdpClientConnectEvent;
import su.plo.voice.api.client.socket.UdpClient;
import su.plo.voice.api.encryption.Encryption;
import su.plo.voice.api.util.Params;
import su.plo.voice.client.BaseVoiceClient;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.client.socket.NettyUdpClient;
import su.plo.voice.proto.data.EncryptionInfo;
import su.plo.voice.proto.packets.tcp.clientbound.*;
import su.plo.voice.proto.packets.tcp.serverbound.PlayerInfoPacket;

import javax.sound.sampled.AudioFormat;
import java.net.InetSocketAddress;
import java.util.Optional;

@AllArgsConstructor
public abstract class BaseServerConnection implements ServerConnection, ClientPacketTcpHandler {

    private static final Logger LOGGER = LogManager.getLogger();

    private final BaseVoiceClient voiceClient;

    @Override
    public void handle(@NotNull ConnectionPacket packet) {
        voiceClient.getUdpClientManager().removeClient(UdpClientClosedEvent.Reason.RECONNECT);

        UdpClient client = new NettyUdpClient(voiceClient, packet.getSecret(), packet.getEncryption());

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
        Encryption encryption = null;
        if (client.get().getEncryptionInfo().isPresent()) {
            EncryptionInfo encryptionInfo = client.get().getEncryptionInfo().get();

            try {
                encryption = voiceClient.getEncryptionManager().create(encryptionInfo.getAlgorithm(), encryptionInfo.getData());
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

        ServerInfo oldServerInfo = voiceClient.getServerInfo().orElse(null);

        voiceClient.setServerInfo(serverInfo);

        // update distances in client config
        // todo: wtf? rework to activation system
        Optional<ClientConfig.Server> configServer = voiceClient.getConfig().getServers().getById(serverInfo.getServerId());
        if (configServer.isPresent()) {
            ClientConfig.Server server = configServer.get();

//            // set default distance
//            int defaultDistance = serverInfo.getVoiceInfo().getDefaultDistance();
//            server.getDistance().setDefault(defaultDistance);
//            if (!serverInfo.getVoiceInfo().getDistances().contains(server.getDistance().value())) {
//                server.getDistance().set(defaultDistance);
//            }
//
//            // set default priority distance
//            int maxPriorityDistance = serverInfo.getVoiceInfo().getMaxPriorityDistance();
//            if (maxPriorityDistance == 0) maxPriorityDistance = Short.MAX_VALUE;
//            int defaultPriorityDistance = Math.min(maxPriorityDistance, serverInfo.getVoiceInfo().getDefaultDistance() * 2);
//            server.getPriorityDistance().setDefault(defaultPriorityDistance);
//
//            if (server.getPriorityDistance().value() > maxPriorityDistance) {
//                server.getPriorityDistance().set(defaultPriorityDistance);
//            }
        } else {
            voiceClient.getConfig().getServers().put(
                    serverInfo.getServerId(),
                    new ClientConfig.Server()
            );

//            // set default distance
//            int defaultDistance = serverInfo.getVoiceInfo().getDefaultDistance();
//            server.getDistance().set(defaultDistance);
//            server.getDistance().setDefault(defaultDistance);
//
//            // set default priority distance
//            int maxPriorityDistance = serverInfo.getVoiceInfo().getMaxPriorityDistance();
//            if (maxPriorityDistance == 0) maxPriorityDistance = Short.MAX_VALUE;
//            int defaultPriorityDistance = Math.min(maxPriorityDistance, serverInfo.getVoiceInfo().getDefaultDistance() * 2);
//            server.getPriorityDistance().setDefault(defaultPriorityDistance);
//            server.getPriorityDistance().set(defaultPriorityDistance);
        }

        // register source lines
        ClientSourceLineManager sourceLines = voiceClient.getSourceLineManager();
        sourceLines.register(serverInfo.getVoiceInfo().getSourceLines());

        // register activations
        ClientActivationManager activations = voiceClient.getActivationManager();
        activations.register(serverInfo.getServerId(), serverInfo.getVoiceInfo().getActivations());

        // initialize capture
        AudioCapture audioCapture = voiceClient.getAudioCapture();
        audioCapture.initialize(serverInfo);
        audioCapture.start();

        // clear & initialize primary output device
        AudioFormat format = new AudioFormat(
                (float) serverInfo.getVoiceInfo().getSampleRate(),
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

        ServerInfoUpdateEvent event = new ServerInfoUpdateEvent(oldServerInfo, serverInfo, packet);
        voiceClient.getEventBus().call(event);
    }

    @Override
    public void handle(@NotNull PlayerInfoRequestPacket packet) {
        sendPacket(new PlayerInfoPacket(voiceClient.getVersion()));
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

    }

    @Override
    public void handle(@NotNull PlayerInfoUpdatePacket packet) {

    }

    @Override
    public void handle(@NotNull SourceAudioEndPacket packet) {
        voiceClient.getSourceManager().getSourceById(packet.getSourceId())
                .ifPresent(source -> source.process(packet));
    }

    @Override
    public void handle(@NotNull SourceInfoPacket packet) {
        voiceClient.getSourceManager().update(packet.getSourceInfo());
    }
}
