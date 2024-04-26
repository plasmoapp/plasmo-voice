//#if MC>=12005
//$$ package su.plo.voice.codec
//$$
//$$ import net.minecraft.network.protocol.common.custom.CustomPacketPayload
//$$ import su.plo.voice.server.ModVoiceServer
//$$
//$$ class PacketServicePayload(
//$$     val data: ByteArray
//$$ ) : CustomPacketPayload {
//$$
//$$     override fun type(): CustomPacketPayload.Type<PacketServicePayload> =
//$$         TYPE
//$$
//$$     companion object {
//$$
//$$         @JvmField
//$$         val TYPE = CustomPacketPayload.Type<PacketServicePayload>(ModVoiceServer.SERVICE_CHANNEL)
//$$     }
//$$ }
//#endif
