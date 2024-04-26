//#if MC>=12005
//$$ package su.plo.voice.codec
//$$
//$$ import net.minecraft.network.RegistryFriendlyByteBuf
//$$ import net.minecraft.network.codec.StreamCodec
//$$
//$$ class PacketServicePayloadCodec : StreamCodec<RegistryFriendlyByteBuf, PacketServicePayload> {
//$$
//$$     override fun decode(buf: RegistryFriendlyByteBuf): PacketServicePayload {
//$$
//$$         val data = ByteArray(buf.readableBytes())
//$$         buf.readBytes(data)
//$$
//$$         return PacketServicePayload(data)
//$$     }
//$$
//$$     override fun encode(buf: RegistryFriendlyByteBuf, payload: PacketServicePayload) {
//$$
//$$         buf.writeBytes(payload.data)
//$$     }
//$$ }
//#endif
