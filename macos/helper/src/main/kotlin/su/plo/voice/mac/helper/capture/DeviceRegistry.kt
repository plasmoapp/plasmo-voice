package su.plo.voice.mac.helper.capture

import kotlinx.cinterop.*
import platform.CoreAudio.*
import platform.CoreAudioTypes.AudioBufferList
import platform.CoreFoundation.*
import platform.darwin.OSStatus
import su.plo.voice.mac.protocol.audio.DeviceInfo

private val WATCHED = listOf(kAudioHardwarePropertyDevices, kAudioHardwarePropertyDefaultInputDevice)

/**
 * The input devices `CoreAudio` knows about.
 */
@OptIn(ExperimentalForeignApi::class)
internal object DeviceRegistry {
    private var listener: StableRef<() -> Unit>? = null

    /** List of devices. */
    fun devices(): List<DeviceInfo> = deviceIds()
        .filter { it.inputChannels() > 0 }
        .mapNotNull { id ->
            val uid = id.stringProperty(kAudioDevicePropertyDeviceUID) ?: return@mapNotNull null
            DeviceInfo(uid, id.stringProperty(kAudioObjectPropertyName) ?: uid)
        }

    /** The input macOS would pick on its own, so the mod does not have to guess. */
    fun defaultId(): String? {
        val device = memScoped {
            val value = alloc<AudioDeviceIDVar>()
            val size = alloc<UIntVar>().apply { this.value = sizeOf<AudioDeviceIDVar>().convert() }

            /*
             * AudioObjectGetPropertyData()
             * https://developer.apple.com/documentation/coreaudio/audioobjectgetpropertydata(_:_:_:_:_:_:)
             */
            val status = AudioObjectGetPropertyData(
                kAudioObjectSystemObject.convert(),
                address(kAudioHardwarePropertyDefaultInputDevice).ptr,
                0u, null, size.ptr, value.ptr,
            )
            if (status != 0) return null

            value.value
        }

        return device.takeIf { it != 0u }?.stringProperty(kAudioDevicePropertyDeviceUID)
    }

    /** Fires on a system thread whenever a device is plugged in, pulled out, or becomes the default. */
    fun onChange(onChanged: () -> Unit) {
        stopListening()

        val reference = StableRef.create(onChanged)
        listener = reference

        memScoped {
            WATCHED.forEach { selector ->
                /*
                 * AudioObjectAddPropertyListener()
                 * https://developer.apple.com/documentation/coreaudio/audioobjectaddpropertylistener(_:_:_:_:)
                 */
                AudioObjectAddPropertyListener(
                    kAudioObjectSystemObject.convert(),
                    address(selector).ptr,
                    devicesChanged,
                    reference.asCPointer(),
                )
            }
        }
    }

    /**
     * `CoreAudio` holds on to the listener until it is told otherwise, so the callback has to outlive
     * nothing: unregister first, then the reference behind it is safe to drop.
     */
    fun stopListening() {
        val reference = listener ?: return
        listener = null

        memScoped {
            WATCHED.forEach { selector ->
                /*
                 * AudioObjectRemovePropertyListener()
                 * https://developer.apple.com/documentation/coreaudio/audioobjectremovepropertylistener(_:_:_:_:)
                 */
                AudioObjectRemovePropertyListener(
                    kAudioObjectSystemObject.convert(),
                    address(selector).ptr,
                    devicesChanged,
                    reference.asCPointer(),
                )
            }
        }

        reference.dispose()
    }

    private fun deviceIds(): List<AudioDeviceID> = memScoped {
        val property = address(kAudioHardwarePropertyDevices)
        val size = alloc<UIntVar>()

        val system: AudioObjectID = kAudioObjectSystemObject.convert()
        /*
         * AudioObjectGetPropertyDataSize()
         * https://developer.apple.com/documentation/coreaudio/audioobjectgetpropertydatasize(_:_:_:_:_:)
         */
        if (AudioObjectGetPropertyDataSize(system, property.ptr, 0u, null, size.ptr) != 0) return emptyList()

        val count = (size.value.toLong() / sizeOf<AudioDeviceIDVar>()).toInt()
        if (count <= 0) return emptyList()

        val ids = allocArray<AudioDeviceIDVar>(count)
        /*
         * AudioObjectGetPropertyData()
         * https://developer.apple.com/documentation/coreaudio/audioobjectgetpropertydata(_:_:_:_:_:_:)
         */
        if (AudioObjectGetPropertyData(system, property.ptr, 0u, null, size.ptr, ids) != 0) return emptyList()

        (0 until count).map { ids[it] }
    }

    /// Zero means the device can only play, so the player has no business seeing it in the list
    private fun AudioDeviceID.inputChannels(): Int = memScoped {
        val property = address(kAudioDevicePropertyStreamConfiguration, kAudioObjectPropertyScopeInput)
        val size = alloc<UIntVar>()
        /*
         * AudioObjectGetPropertyDataSize()
         * https://developer.apple.com/documentation/coreaudio/audioobjectgetpropertydatasize(_:_:_:_:_:)
         */
        if (AudioObjectGetPropertyDataSize(this@inputChannels, property.ptr, 0u, null, size.ptr) != 0) return 0

        val bytes = allocArray<ByteVar>(size.value.toInt())
        /*
         * AudioObjectGetPropertyData()
         * https://developer.apple.com/documentation/coreaudio/audioobjectgetpropertydata(_:_:_:_:_:_:)
         */
        if (AudioObjectGetPropertyData(this@inputChannels, property.ptr, 0u, null, size.ptr, bytes) != 0) return 0

        val list = bytes.reinterpret<AudioBufferList>().pointed
        val buffers = list.mBuffers
        (0 until list.mNumberBuffers.toInt()).sumOf { buffers[it].mNumberChannels.toInt() }
    }

    private fun AudioObjectID.stringProperty(selector: AudioObjectPropertySelector): String? = memScoped {
        val value = alloc<CFStringRefVar>()
        val size = alloc<UIntVar>().apply { this.value = sizeOf<CFStringRefVar>().convert() }

        /*
         * AudioObjectGetPropertyData()
         * https://developer.apple.com/documentation/coreaudio/audioobjectgetpropertydata(_:_:_:_:_:_:)
         */
        if (AudioObjectGetPropertyData(this@stringProperty, address(selector).ptr, 0u, null, size.ptr, value.ptr) != 0) {
            return null
        }

        try {
            value.value?.asString()
        } finally {
            /*
             * CFRelease()
             * https://developer.apple.com/documentation/corefoundation/cfrelease
             */
            value.value?.let { CFRelease(it) }
        }
    }

    private fun MemScope.address(
        selector: AudioObjectPropertySelector,
        scope: AudioObjectPropertyScope = kAudioObjectPropertyScopeGlobal,
    ) = alloc<AudioObjectPropertyAddress>().apply {
        mSelector = selector
        mScope = scope
        mElement = kAudioObjectPropertyElementMain
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun CFStringRef.asString(): String = memScoped {
    /*
     * CFStringGetLength()
     * https://developer.apple.com/documentation/corefoundation/cfstringgetlength(_:)
     */
    val length = CFStringGetLength(this@asString)
    val capacity = length * 4 + 1 // Worst case
    val bytes = allocArray<ByteVar>(capacity)

    /*
     * CFStringGetCString()
     * https://developer.apple.com/documentation/corefoundation/cfstringgetcstring(_:_:_:_:)
     */
    if (CFStringGetCString(this@asString, bytes, capacity, kCFStringEncodingUTF8)) bytes.toKString() else ""
}

/*
 * AudioObjectPropertyListenerProc
 * https://developer.apple.com/documentation/coreaudio/audioobjectpropertylistenerproc
 */
@OptIn(ExperimentalForeignApi::class)
private val devicesChanged = staticCFunction<
        AudioObjectID,
        UInt,
        CPointer<AudioObjectPropertyAddress>?,
        COpaquePointer?,
        OSStatus,
        > { _, _, _, clientData ->
    clientData?.asStableRef<() -> Unit>()?.get()?.invoke()
    0
}
