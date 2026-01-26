package su.plo.voice.proto.packets

enum class PacketDirection {
    CLIENT,
    SERVER,
    ANY;

    fun accepts(direction: PacketDirection): Boolean =
        direction == ANY || this == ANY || direction == this
}
