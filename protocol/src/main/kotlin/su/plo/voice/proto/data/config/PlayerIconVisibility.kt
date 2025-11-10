package su.plo.voice.proto.data.config

import java.util.EnumSet

/**
 * Flags to control icon visibility about players heads.
 *
 * Note that this only controls visibility over PLAYERS heads,
 * to hide icons above static / entity use appropriate Source#setIconVisible.
 *
 * Icon names documented are exactly the same as in this screenshot: https://i.imgur.com/JbWCjLK.png.
 */
enum class PlayerIconVisibility {
    /**
     * Hides "No Mod Installed" icon.
     *
     * This icon is visible when player doesn't have Plasmo Voice installed.
     */
    HIDE_NOT_INSTALLED,

    /**
     * Hides "Player Muted Audio" icon.
     *
     * This icon is visible when player disables voice chat on the client.
     */
    HIDE_VOICE_CHAT_DISABLED,

    /**
     * Hides "Server Muted" icon.
     *
     * This icon is visible when player's voice chat is muted on the server.
     */
    HIDE_SERVER_MUTED,

    /**
     * Hides "Client Muted" icon.
     *
     * This icon is visible when player is muted on the client using "Volume" tab.
     */
    HIDE_CLIENT_MUTED,

    /**
     * Hides "Player Client Audio" icon.
     *
     * This icon is visible when source attached to the player is activated.
     * (e.g. player talking in the proximity)
     */
    HIDE_SOURCE_ICON;

    companion object {
        @JvmStatic
        fun of(collection: Collection<PlayerIconVisibility>): EnumSet<PlayerIconVisibility> =
            if (collection.isEmpty())
                none()
            else
                EnumSet.copyOf(collection)

        @JvmStatic
        fun none(): EnumSet<PlayerIconVisibility> = EnumSet.noneOf(PlayerIconVisibility::class.java)

        @JvmStatic
        fun all(): EnumSet<PlayerIconVisibility> = EnumSet.allOf(PlayerIconVisibility::class.java)
    }
}
