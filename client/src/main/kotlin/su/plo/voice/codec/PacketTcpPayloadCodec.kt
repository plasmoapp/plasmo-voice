//#if MC>=12005
//$$ package su.plo.voice.codec
//$$
//$$ import com.google.common.io.ByteStreams
//$$ import net.minecraft.network.RegistryFriendlyByteBuf
//$$ import net.minecraft.network.codec.StreamCodec
//$$ import su.plo.voice.proto.packets.PacketHandler
//$$ import su.plo.voice.proto.packets.tcp.PacketTcpCodec
//$$ import java.io.IOException
//$$
//$$ class PacketTcpPayloadCodec : StreamCodec<RegistryFriendlyByteBuf, PacketTcpPayload> {
//$$
//$$     override fun decode(buf: RegistryFriendlyByteBuf): PacketTcpPayload {
//$$
//$$         val data = ByteArray(buf.readableBytes())
//$$         buf.readBytes(data)
//$$
//$$         val packet = PacketTcpCodec.decode<PacketHandler>(ByteStreams.newDataInput(data))
//$$             .orElseThrow { IOException("Bad packet") }
//$$
//$$         return PacketTcpPayload(packet)
//$$     }
//$$
//$$     override fun encode(buf: RegistryFriendlyByteBuf, payload: PacketTcpPayload) {
//$$
//$$         val encoded = PacketTcpCodec.encode(payload.packet)
//$$
//$$         buf.writeBytes(encoded)
//$$     }
//$$ }
//#endif
