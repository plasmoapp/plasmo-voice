package su.plo.voice.client.mac

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.NativeLong
import com.sun.jna.Pointer

// https://stackoverflow.com/questions/13719269/calling-objective-c-method-from-java
interface AVFoundation : Library {

    /*
     * objc_getClass()
     * https://developer.apple.com/documentation/objectivec/objc_getclass(_:)
     */
    fun objc_getClass(className: String): Pointer

    /*
     * sel_registerName()
     * https://developer.apple.com/documentation/objectivec/sel_registername(_:)
     */
    fun sel_registerName(selectorName: String): Pointer

    /*
     * objc_msgSend()
     * https://developer.apple.com/documentation/objectivec/objc_msgsend
     */
    fun objc_msgSend(receiver: Pointer, selector: Pointer, pointer: NativeLong): NativeLong

    /*
     * objc_msgSend()
     * https://developer.apple.com/documentation/objectivec/objc_msgsend
     */
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
