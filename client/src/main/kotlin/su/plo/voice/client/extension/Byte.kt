package su.plo.voice.client.extension

fun Byte.diff(other: Byte): Byte =
    if (other > this) {
        (other - this).toByte()
    } else {
        (Byte.MAX_VALUE - this + (other - Byte.MIN_VALUE) + 1).toByte()
    }
