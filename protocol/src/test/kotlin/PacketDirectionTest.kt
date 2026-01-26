import su.plo.voice.proto.packets.PacketDirection
import kotlin.test.Test
import kotlin.test.assertEquals

class PacketDirectionTest {
    @Test
    fun accepts() {
        assertEquals(true, PacketDirection.CLIENT.accepts(PacketDirection.CLIENT))
        assertEquals(true, PacketDirection.CLIENT.accepts(PacketDirection.ANY))
        assertEquals(false, PacketDirection.CLIENT.accepts(PacketDirection.SERVER))

        assertEquals(true, PacketDirection.SERVER.accepts(PacketDirection.SERVER))
        assertEquals(true, PacketDirection.SERVER.accepts(PacketDirection.ANY))
        assertEquals(false, PacketDirection.SERVER.accepts(PacketDirection.CLIENT))

        assertEquals(true, PacketDirection.ANY.accepts(PacketDirection.SERVER))
        assertEquals(true, PacketDirection.ANY.accepts(PacketDirection.CLIENT))
        assertEquals(true, PacketDirection.ANY.accepts(PacketDirection.ANY))
    }
}
