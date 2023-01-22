package su.plo.voice.proto.packets.tcp.clientbound;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.proto.packets.tcp.PacketTcpHandler;

public interface ClientPacketTcpHandler extends PacketTcpHandler {

    void handle(@NotNull ConnectionPacket packet);

    void handle(@NotNull ConfigPlayerInfoPacket packet);

    void handle(@NotNull PlayerInfoRequestPacket packet);

    void handle(@NotNull ConfigPacket packet);

    void handle(@NotNull PlayerListPacket packet);

    void handle(@NotNull PlayerInfoUpdatePacket packet);

    void handle(@NotNull PlayerDisconnectPacket packet);

    void handle(@NotNull SourceAudioEndPacket packet);

    void handle(@NotNull SourceInfoPacket packet);

    void handle(@NotNull SelfSourceInfoPacket packet);

    void handle(@NotNull SourceLineRegisterPacket packet);

    void handle(@NotNull SourceLineUnregisterPacket packet);

    void handle(@NotNull SourceLinePlayerAddPacket packet);

    void handle(@NotNull SourceLinePlayerRemovePacket packet);

    void handle(@NotNull SourceLinePlayersListPacket packet);

    void handle(@NotNull ActivationRegisterPacket packet);

    void handle(@NotNull ActivationUnregisterPacket packet);

    void handle(@NotNull DistanceVisualizePacket packet);
}
