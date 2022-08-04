package su.plo.voice.client.connection;

import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.client.connection.ServerInfo;
import su.plo.voice.api.client.event.connection.ServerInfoUpdateEvent;
import su.plo.voice.api.client.event.socket.UdpClientClosedEvent;
import su.plo.voice.api.client.event.socket.UdpClientConnectEvent;
import su.plo.voice.api.client.socket.UdpClient;
import su.plo.voice.client.BaseVoiceClient;
import su.plo.voice.client.socket.NettyUdpClient;
import su.plo.voice.proto.packets.tcp.clientbound.ClientPacketTcpHandler;
import su.plo.voice.proto.packets.tcp.clientbound.ConfigPacket;
import su.plo.voice.proto.packets.tcp.clientbound.ConfigPlayerInfoPacket;
import su.plo.voice.proto.packets.tcp.clientbound.ConnectionPacket;

import java.net.InetSocketAddress;
import java.util.Optional;

@AllArgsConstructor
public abstract class BaseClientPacketTcpHandler implements ClientPacketTcpHandler {

    private final Logger logger = LogManager.getLogger(BaseClientPacketTcpHandler.class);

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
            logger.error("Failed to connect to the UDP server", e);
        }
    }

    @Override
    public void handle(@NotNull ConfigPacket packet) {
        Optional<UdpClient> client = voiceClient.getUdpClientManager().getClient();
        if (!client.isPresent()) {
            logger.warn("Config packet is received before UDP is connected");
            return;
        }

        Optional<InetSocketAddress> remoteAddress = client.get().getRemoteAddress();
        if (!remoteAddress.isPresent()) {
            logger.warn("Config packet is received before UDP is connected");
            return;
        }

        ServerInfo serverInfo = new VoiceServerInfo(
                client.get().getSecret(),
                remoteAddress.get(),
                client.get().getEncryptionInfo().orElse(null),
                packet
        );

        ServerInfo oldServerInfo = voiceClient.getCurrentServerInfo().orElse(null);

        voiceClient.setCurrentServerInfo(serverInfo);

        ServerInfoUpdateEvent event = new ServerInfoUpdateEvent(oldServerInfo, serverInfo, packet);
        voiceClient.getEventBus().call(event);
    }

    @Override
    public void handle(@NotNull ConfigPlayerInfoPacket packet) {

    }
}
