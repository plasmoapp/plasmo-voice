package su.plo.voice.client.mac

import com.sun.jna.NativeLong

enum class AVAuthorizationStatus(val value: Int) {

    NOT_DETERMINED(0),
    RESTRICTED(1),
    DENIED(2),
    AUTHORIZED(3);

    companion object {

        fun fromValue(value: NativeLong) =
            fromValue(value.toInt())

        fun fromValue(value: Int): AVAuthorizationStatus =
            values().find { it.value == value } ?: NOT_DETERMINED
    }
}
