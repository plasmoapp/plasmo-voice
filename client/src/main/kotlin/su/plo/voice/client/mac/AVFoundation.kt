package su.plo.voice.client.mac

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.NativeLong
import com.sun.jna.Pointer

// https://stackoverflow.com/questions/13719269/calling-objective-c-method-from-java
interface AVFoundation : Library {

    // https://developer.apple.com/documentation/objectivec/1418952-objc_getclass?language=objc
    fun objc_getClass(className: String): Pointer

    // https://developer.apple.com/documentation/objectivec/1418557-sel_registername?language=objc
    fun sel_registerName(selectorName: String): Pointer

    // https://developer.apple.com/documentation/objectivec/1456712-objc_msgsend?language=objc
    fun objc_msgSend(receiver: Pointer, selector: Pointer, pointer: NativeLong): NativeLong

    // https://developer.apple.com/documentation/objectivec/1456712-objc_msgsend?language=objc
    fun objc_msgSend(receiver: Pointer, selector: Pointer, pointer1: NativeLong, pointer2: Pointer?): NativeLong

    companion object {
        val INSTANCE: AVFoundation by lazy {
            //#if MC>=11700
            Native.load("AVFoundation", AVFoundation::class.java)
            //#else
            //$$ throw IllegalStateException("AVFoundation not supported on this version of JNA")
            //#endif
        }
    }
}
