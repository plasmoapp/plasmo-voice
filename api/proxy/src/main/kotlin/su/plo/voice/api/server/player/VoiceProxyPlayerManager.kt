package su.plo.voice.api.server.player

import su.plo.voice.api.proxy.player.VoiceProxyPlayer
import su.plo.voice.api.server.connection.ConnectionManager
import su.plo.voice.proto.packets.tcp.clientbound.ClientPacketTcpHandler

interface VoiceProxyPlayerManager : VoicePlayerManager<VoiceProxyPlayer>,
    ConnectionManager<ClientPacketTcpHandler, VoiceProxyPlayer>
