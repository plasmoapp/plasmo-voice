package su.plo.voice.client.mac

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.NativeLong
import com.sun.jna.Pointer

// https://stackoverflow.com/questions/13719269/calling-objective-c-method-from-java
interface Foundation : Library {

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
    fun objc_msgSend(receiver: Pointer, selector: Pointer, string: String): NativeLong

    companion object {
        val INSTANCE: Foundation by lazy {
            //#if MC>=11700
            Native.load("Foundation", Foundation::class.java)
            //#else
            //$$ Native.loadLibrary("Foundation", Foundation::class.java)
            //#endif
        }

        fun getNSString(string: String): NativeLong {
            return INSTANCE.objc_msgSend(
                INSTANCE.objc_getClass("NSString"),
                INSTANCE.sel_registerName("stringWithUTF8String:"),
                string
            )
        }
    }
}
