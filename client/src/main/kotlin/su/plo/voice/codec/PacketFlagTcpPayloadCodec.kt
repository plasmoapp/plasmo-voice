//#if MC>=12005
//$$ package su.plo.voice.codec
//$$
//$$ import net.minecraft.network.RegistryFriendlyByteBuf
//$$ import net.minecraft.network.codec.StreamCodec
//$$
//$$ class PacketFlagTcpPayloadCodec : StreamCodec<RegistryFriendlyByteBuf, PacketFlagTcpPayload> {
//$$
//$$     override fun decode(buf: RegistryFriendlyByteBuf): PacketFlagTcpPayload =
//$$         PacketFlagTcpPayload() // do nothing, empty packet
//$$
//$$     override fun encode(buf: RegistryFriendlyByteBuf, payload: PacketFlagTcpPayload) {
//$$     // do nothing, empty packet
//$$     }
//$$ }
//#endif
