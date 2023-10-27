package su.plo.voice.client.mac

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.NativeLong
import com.sun.jna.Pointer

// https://stackoverflow.com/questions/13719269/calling-objective-c-method-from-java
interface Foundation : Library {

    // https://developer.apple.com/documentation/objectivec/1418952-objc_getclass?language=objc
    fun objc_getClass(className: String): Pointer

    // https://developer.apple.com/documentation/objectivec/1418557-sel_registername?language=objc
    fun sel_registerName(selectorName: String): Pointer

    // https://developer.apple.com/documentation/objectivec/1456712-objc_msgsend?language=objc
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
