package su.plo.voice.util

fun getIntSystemProperty(name: String, default: Int): Int =
    System.getProperty(name)?.toInt() ?: default
