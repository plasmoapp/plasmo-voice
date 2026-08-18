package su.plo.voice.api.client.config

/**
 * Which capture implementation the player wants.
 *
 * [AUTO] means "whatever suits this machine", which on macOS is the helper app, because
 * it is the only one that can hold a microphone permission of its own.
 */
enum class InputBackend(val factoryName: String) {
    AUTO(""),
    OPEN_AL("AL_INPUT"),
    JAVAX("JAVAX_INPUT"),
    COREAUDIO("COREAUDIO_INPUT")
}
