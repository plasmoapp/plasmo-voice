package su.plo.lib.paper

import com.google.common.collect.ImmutableList
import com.google.common.collect.Maps
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.World
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import su.plo.lib.api.server.MinecraftServerLib
import su.plo.lib.api.server.entity.MinecraftServerEntity
import su.plo.lib.api.server.entity.MinecraftServerPlayerEntity
import su.plo.lib.api.server.event.player.PlayerJoinEvent
import su.plo.lib.api.server.event.player.PlayerQuitEvent
import su.plo.lib.api.server.permission.PermissionsManager
import su.plo.lib.api.server.world.MinecraftServerWorld
import su.plo.lib.paper.chat.BaseComponentTextConverter
import su.plo.lib.paper.command.PaperCommandManager
import su.plo.lib.paper.entity.PaperServerEntity
import su.plo.lib.paper.entity.PaperServerPlayer
import su.plo.lib.paper.world.PaperServerWorld
import su.plo.voice.api.server.config.ServerLanguages
import su.plo.voice.proto.data.player.MinecraftGameProfile
import su.plo.voice.server.player.PermissionSupplier
import java.util.*
import java.util.function.Supplier

class PaperServerLib(
    private val loader: JavaPlugin,
    languagesSupplier: Supplier<ServerLanguages?>
) : MinecraftServerLib, Listener {

    var permissions: PermissionSupplier? = null

    private val worldByInstance: MutableMap<World, MinecraftServerWorld> = Maps.newConcurrentMap()
    private val playerById: MutableMap<UUID, MinecraftServerPlayerEntity> = Maps.newConcurrentMap()

    private val textConverter: BaseComponentTextConverter
    private val commandManager: PaperCommandManager
    private val permissionsManager = PermissionsManager()

    init {
        textConverter = BaseComponentTextConverter(languagesSupplier)
        commandManager = PaperCommandManager(this, textConverter)
    }

    override fun onShutdown() {
        permissions = null
        commandManager.clear()
        permissionsManager.clear()
    }

    override fun getTextConverter() = textConverter

    override fun getCommandManager() = commandManager

    override fun getPermissionsManager() = permissionsManager

    override fun executeInMainThread(runnable: Runnable) {
        Bukkit.getServer().scheduler.runTask(loader, runnable)
    }

    override fun getWorld(instance: Any): MinecraftServerWorld {
        require(instance is World) { "instance is not ${World::class.java}" }

        return worldByInstance.computeIfAbsent(
            instance
        ) { PaperServerWorld(instance, loader) }
    }

    override fun getWorlds(): Collection<MinecraftServerWorld> =
        if (Bukkit.getWorlds().size == worldByInstance.size) {
            worldByInstance.values
        } else {
            Bukkit.getWorlds().map(::getWorld)
        }

    override fun getPlayerByInstance(instance: Any): MinecraftServerPlayerEntity {
        require(instance is Player) { "instance is not ${Player::class.java}" }

        var serverPlayer = playerById[instance.uniqueId]
        if ((serverPlayer?.getInstance() as? Player)?.entityId != instance.entityId) {
            serverPlayer = PaperServerPlayer(
                loader,
                this,
                textConverter,
                permissions!!,
                instance
            )

            playerById[instance.uniqueId] = serverPlayer
        }

        return serverPlayer
    }

    override fun getPlayerByName(name: String): Optional<MinecraftServerPlayerEntity> {
        val player = Bukkit.getPlayer(name) ?: return Optional.empty()
        return Optional.of(getPlayerByInstance(player))
    }

    override fun getPlayerById(playerId: UUID): Optional<MinecraftServerPlayerEntity> {
        val serverPlayer = playerById[playerId]
        if (serverPlayer != null) return Optional.of(serverPlayer)

        val player = Bukkit.getPlayer(playerId) ?: return Optional.empty()

        return Optional.of(getPlayerByInstance(player))
    }

    override fun getGameProfile(playerId: UUID): Optional<MinecraftGameProfile> {
        return Optional.of(Bukkit.getServer().getOfflinePlayer(playerId))
            .filter(OfflinePlayer::hasPlayedBefore)
            .map(::getGameProfile)
    }

    override fun getGameProfile(name: String): Optional<MinecraftGameProfile> {
        return Optional.of(Bukkit.getServer().getOfflinePlayer(name))
            .filter(OfflinePlayer::hasPlayedBefore)
            .map(::getGameProfile)
    }

    private fun getGameProfile(offlinePlayer: OfflinePlayer): MinecraftGameProfile {
        // todo: use game profile properties?
        return MinecraftGameProfile(offlinePlayer.uniqueId, offlinePlayer.name, ImmutableList.of())
    }

    override fun getPlayers() = playerById.values

    override fun getEntity(instance: Any): MinecraftServerEntity {
        require(instance is LivingEntity) { "instance is not ${LivingEntity::class.java}" }

        return PaperServerEntity(
            this,
            instance
        )
    }

    override fun getPort() = Bukkit.getServer().port

    override fun getVersion() = Bukkit.getMinecraftVersion()

    @EventHandler
    fun onPlayerJoin(event: org.bukkit.event.player.PlayerJoinEvent) {
        PlayerJoinEvent.invoker.onPlayerJoin(
            getPlayerByInstance(event.player)
        )
    }

    @EventHandler
    fun onPlayerQuit(event: org.bukkit.event.player.PlayerQuitEvent) {
        PlayerQuitEvent.invoker.onPlayerQuit(
            getPlayerByInstance(event.player)
        )
        playerById.remove(event.player.uniqueId)
    }
}
