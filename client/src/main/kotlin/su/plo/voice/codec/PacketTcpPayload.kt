//#if MC>=12005
//$$ package su.plo.voice.codec
//$$
//$$ import net.minecraft.network.protocol.common.custom.CustomPacketPayload
//$$ import su.plo.voice.proto.packets.Packet
//$$ import su.plo.voice.proto.packets.PacketHandler
//$$ import su.plo.voice.server.ModVoiceServer
//$$
//$$ class PacketTcpPayload(
//$$     val packet: Packet<PacketHandler>
//$$ ) : CustomPacketPayload {
//$$
//$$     override fun type(): CustomPacketPayload.Type<PacketTcpPayload> =
//$$         TYPE
//$$
//$$     companion object {
//$$
//$$         @JvmField
//$$         val TYPE = CustomPacketPayload.Type<PacketTcpPayload>(ModVoiceServer.CHANNEL)
//$$     }
//$$ }
//#endif
