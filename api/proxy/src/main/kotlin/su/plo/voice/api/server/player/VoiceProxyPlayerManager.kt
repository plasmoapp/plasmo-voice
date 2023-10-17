package su.plo.voice.api.server.player

import su.plo.voice.api.proxy.player.VoiceProxyPlayer
import su.plo.voice.api.server.connection.PacketManager
import su.plo.voice.proto.packets.tcp.clientbound.ClientPacketTcpHandler

/**
 * Manages voice proxy players.
 *
 * Use this manager to get [VoiceProxyPlayer].
 */
interface VoiceProxyPlayerManager : VoicePlayerManager<VoiceProxyPlayer>,
    PacketManager<ClientPacketTcpHandler, VoiceProxyPlayer>
