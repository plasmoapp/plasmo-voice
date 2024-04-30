//#if MC>=12005
//$$ package su.plo.voice.codec
//$$
//$$ import net.minecraft.network.protocol.common.custom.CustomPacketPayload
//$$ import su.plo.voice.server.ModVoiceServer
//$$
//$$ class PacketFlagTcpPayload : CustomPacketPayload {
//$$
//$$     override fun type(): CustomPacketPayload.Type<PacketFlagTcpPayload> =
//$$         TYPE
//$$
//$$     companion object {
//$$
//$$         @JvmField
//$$         val TYPE = CustomPacketPayload.Type<PacketFlagTcpPayload>(ModVoiceServer.FLAG_CHANNEL)
//$$     }
//$$ }
//#endif
