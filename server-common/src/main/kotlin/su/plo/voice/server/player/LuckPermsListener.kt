package su.plo.voice.server.player

import com.google.common.collect.Maps
import net.luckperms.api.LuckPerms
import net.luckperms.api.LuckPermsProvider
import net.luckperms.api.event.EventSubscription
import net.luckperms.api.event.node.NodeAddEvent
import net.luckperms.api.event.node.NodeClearEvent
import net.luckperms.api.event.node.NodeMutateEvent
import net.luckperms.api.event.node.NodeRemoveEvent
import net.luckperms.api.event.user.UserDataRecalculateEvent
import net.luckperms.api.model.group.Group
import net.luckperms.api.model.user.User
import net.luckperms.api.node.Node
import su.plo.voice.api.server.PlasmoBaseVoiceServer
import su.plo.voice.api.server.event.player.PlayerPermissionUpdateEvent
import su.plo.voice.api.server.player.VoicePlayer
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class LuckPermsListener(
    private val voiceServer: PlasmoBaseVoiceServer,
    private val executor: ScheduledExecutorService
) {

    private val luckPerms: LuckPerms = LuckPermsProvider.get()
    private val subscriptions: MutableList<EventSubscription<*>> = ArrayList()
    private val permissionChanges: MutableMap<String, ScheduledFuture<*>> = Maps.newHashMap()

    fun subscribe() {
        val luckPermsBus = luckPerms.eventBus

        subscriptions.add(luckPermsBus.subscribe(
            NodeAddEvent::class.java,
            ::onNodeAdd
        ))

        subscriptions.add(luckPermsBus.subscribe(
            NodeRemoveEvent::class.java,
            ::onNodeRemove
        ))

        subscriptions.add(luckPermsBus.subscribe(
            NodeClearEvent::class.java,
            ::onNodeClear
        ))
    }

    fun unsubscribe() {
        subscriptions.forEach { it.close() }
    }

    private fun onNodeAdd(event: NodeAddEvent) {
        onNodeMutate(event, event.node)
    }

    private fun onNodeRemove(event: NodeRemoveEvent) {
        onNodeMutate(event, event.node)
    }

    private fun onNodeClear(event: NodeClearEvent) {
        event.nodes.forEach {
            onNodeMutate(event, it)
        }
    }

    private fun onNodeMutate(event: NodeMutateEvent, node: Node) {
        if (event.isUser) {
            val user = event.target as User

            val player = voiceServer.playerManager.getPlayerById(user.uniqueId, false).orElse(null) ?: return
            if (!player.hasVoiceChat()) return

            onLpPermissionChange(player, node.key)
        } else if (event.isGroup) {
            val group = event.target as Group

            // iterate all online players and check if they are in group
            voiceServer.playerManager.players.forEach { player ->
                if (player.instance.hasPermission("group." + group.name)) {
                    onLpPermissionChange(player, node.key)
                }
            }
        }
    }

    @Synchronized
    private fun onLpPermissionChange(player: VoicePlayer, permission: String) {
        val playerPermissionKey: String = player.instance.uuid.toString() + "_" + permission
        val future = permissionChanges[playerPermissionKey]
        future?.cancel(false)

        permissionChanges[player.instance.uuid.toString() + "_" + permission] = executor.schedule(
            { onPermissionChange(player, permission) },
            100L,
            TimeUnit.MILLISECONDS
        )
    }

    private fun onPermissionChange(player: VoicePlayer, permission: String) {
        voiceServer.eventBus.call(PlayerPermissionUpdateEvent(player, permission))
        permissionChanges.remove(player.instance.uuid.toString() + "_" + permission)
    }

    companion object {

        fun hasLuckPerms() =
            try {
                Class.forName("net.luckperms.api.LuckPermsProvider")
                true
            } catch (_: Exception) {
                false
            }
    }
}
