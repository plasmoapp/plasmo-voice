package su.plo.voice.server.command

import su.plo.slib.api.command.McCommandSource
import su.plo.slib.api.permission.PermissionDefault

enum class Permission(
    val key: String,
    val defaultValue: PermissionDefault,
) {
    MUTE("pv.mute", PermissionDefault.OP),
    UNMUTE("pv.unmute", PermissionDefault.OP),
    MUTE_LIST("pv.mutelist", PermissionDefault.OP),

    RELOAD("pv.reload", PermissionDefault.OP),

    RECONNECT("pv.reconnect", PermissionDefault.TRUE),

    LIST("pv.list", PermissionDefault.TRUE),

    ALLOW_FREECAM("pv.allow_freecam", PermissionDefault.TRUE),
}

fun McCommandSource.hasPermission(permission: Permission): Boolean =
    hasPermission(permission.key)
