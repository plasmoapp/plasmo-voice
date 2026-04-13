import su.plo.voice.proto.packets.PacketDirection
import su.plo.voice.proto.packets.PacketRegistry
import su.plo.voice.proto.packets.udp.bothbound.PingPacket
import kotlin.test.Test
import kotlin.test.assertNull

class PacketRegistryTest {
    @Test
    fun `byType returns null for unregistered packet id`() {
        val registry = PacketRegistry()
        registry.register(0x01, PacketDirection.ANY, PingPacket::class.java, ::PingPacket)

        assertNull(registry.byType(0xFF, PacketDirection.CLIENT))
    }

    @Test
    fun `byType returns null for wrong direction`() {
        val registry = PacketRegistry()
        registry.register(0x01, PacketDirection.CLIENT, PingPacket::class.java, ::PingPacket)

        assertNull(registry.byType(0x01, PacketDirection.SERVER))
    }

    @Test
    fun `byType returns packet for matching direction`() {
        val registry = PacketRegistry()
        registry.register(0x01, PacketDirection.CLIENT, PingPacket::class.java, ::PingPacket)

        assert(registry.byType(0x01, PacketDirection.CLIENT) is PingPacket)
        assert(registry.byType(0x01, PacketDirection.ANY) is PingPacket)
    }
}
