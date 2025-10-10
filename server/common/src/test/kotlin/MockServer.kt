import com.google.common.collect.Maps
import com.google.common.collect.Multimaps
import com.google.common.collect.SetMultimap
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import su.plo.slib.api.chat.component.McTextComponent
import su.plo.slib.api.chat.converter.ServerTextConverter
import su.plo.slib.api.command.McCommand
import su.plo.slib.api.command.McCommandManager
import su.plo.slib.api.entity.player.McGameProfile
import su.plo.slib.api.event.player.McPlayerJoinEvent
import su.plo.slib.api.language.ServerTranslator
import su.plo.slib.api.logging.McLogger
import su.plo.slib.api.logging.McLoggerFactory
import su.plo.slib.api.permission.PermissionManager
import su.plo.slib.api.permission.PermissionTristate
import su.plo.slib.api.position.Pos3d
import su.plo.slib.api.server.McServerLib
import su.plo.slib.api.server.channel.McServerChannelHandler
import su.plo.slib.api.server.channel.McServerChannelManager
import su.plo.slib.api.server.entity.McServerEntity
import su.plo.slib.api.server.entity.player.McServerPlayer
import su.plo.slib.api.server.position.ServerPos3d
import su.plo.slib.api.server.scheduler.McServerScheduler
import su.plo.slib.api.server.world.McServerWorld
import su.plo.voice.server.BaseVoiceServer
import su.plo.voice.util.version.PlatformLoader
import java.io.File
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Supplier
import java.util.logging.ConsoleHandler
import java.util.logging.Level
import java.util.logging.Logger
import java.util.logging.SimpleFormatter

class MainThreadExecutor(
    private val executor: ExecutorService,
) : McServerScheduler {
    override fun <T> runTask(
        task: Supplier<T>,
        loader: Any?,
    ): CompletableFuture<T> =
        CompletableFuture.supplyAsync(
            { task.get() },
            executor,
        )

    override fun <T> runTaskFor(
        entity: McServerEntity,
        task: Supplier<T>,
        loader: Any?,
    ): CompletableFuture<T> =
        CompletableFuture.supplyAsync(
            { task.get() },
            executor,
        )

    override fun <T> runTaskAt(
        location: ServerPos3d,
        task: Supplier<T>,
        loader: Any?,
    ): CompletableFuture<T> =
        CompletableFuture.supplyAsync(
            { task.get() },
            executor,
        )
}

fun mockWorld(worldName: String) = mock<McServerWorld> {
    on { name } doReturn worldName
}

open class MockServerPlayer(
    override val world: McServerWorld,
    position: Pos3d,
) : McServerPlayer {
    private val entityId = AtomicInteger(1)

    override val isSpectator: Boolean = false
    override val isSneaking: Boolean = false
    override val hasLabelScoreboard: Boolean = false
    override val registeredChannels: Collection<String> = listOf(BaseVoiceServer.CHANNEL_STRING, BaseVoiceServer.FLAG_CHANNEL_STRING)
    override val spectatorTarget: McServerEntity? = null

    override val isOnline: Boolean = true
    override val gameProfile: McGameProfile =
        UUID.randomUUID().let { playerId ->
            McGameProfile(
                playerId,
                playerId.toString(),
                emptyList(),
            )
        }
    override val uuid: UUID = gameProfile.id
    override val name: String = gameProfile.name

    override val id: Int = entityId.andIncrement
    override val eyeHeight: Double = 2.0
    override val hitBoxWidth: Float = 1.0f
    override val hitBoxHeight: Float = 2.0f

    override val language: String = "en_us"

    private val serverPosition = ServerPos3d(
        world,
        position.x,
        position.y,
        position.z,
    )
    private val lookAngle = Pos3d()

    override fun canSee(player: McServerPlayer): Boolean = true

    @Suppress("UNCHECKED_CAST")
    override fun <T> getInstance(): T = this as T

    override fun sendPacket(channel: String, data: ByteArray) {
        throw NotImplementedError()
    }

    override fun kick(reason: McTextComponent) {
    }

    override fun hasPermission(permission: String): Boolean = true

    override fun getPermission(permission: String): PermissionTristate = PermissionTristate.TRUE

    override fun sendMessage(text: McTextComponent) {
    }

    override fun sendActionBar(text: McTextComponent) {
    }

    override fun isValid(): Boolean = true

    override fun getServerPosition(): ServerPos3d = serverPosition

    override fun getServerPosition(position: ServerPos3d): ServerPos3d {
        position.world = world
        position.x = serverPosition.x
        position.y = serverPosition.y
        position.z = serverPosition.z
        position.yaw = serverPosition.yaw
        position.pitch = serverPosition.pitch

        return position
    }

    override fun getPosition(): Pos3d = serverPosition.toPosition()

    override fun getPosition(position: Pos3d): Pos3d {
        position.x = serverPosition.x
        position.y = serverPosition.y
        position.z = serverPosition.z

        return position
    }


    override fun getLookAngle() = getLookAngle(lookAngle)

    override fun getLookAngle(lookAngle: Pos3d): Pos3d {
        lookAngle.x = this.lookAngle.x
        lookAngle.y = this.lookAngle.y
        lookAngle.z = this.lookAngle.z

        return lookAngle
    }
}

class JavaLogger(
    name: String
) : Logger(name, null), McLogger {

    init {
        useParentHandlers = false

        val consoleHandler = ConsoleHandler()
        consoleHandler.level = Level.INFO
        consoleHandler.formatter = SimpleFormatter()
        addHandler(consoleHandler)
    }

    override fun trace(format: String, vararg arguments: Any?) {
        log(Level.FINEST, String.format(format.convertFromSlf4jFormat(), *arguments))
        arguments.printStackTrace()
    }

    override fun debug(format: String, vararg arguments: Any?) {
        log(Level.ALL, String.format(format.convertFromSlf4jFormat(), *arguments))
        arguments.printStackTrace()
    }

    override fun info(format: String, vararg arguments: Any?) {
        log(Level.INFO, String.format(format.convertFromSlf4jFormat(), *arguments))
        arguments.printStackTrace()
    }

    override fun warn(format: String, vararg arguments: Any?) {
        log(Level.WARNING, String.format(format.convertFromSlf4jFormat(), *arguments))
        arguments.printStackTrace()
    }

    override fun error(format: String, vararg arguments: Any?) {
        log(Level.SEVERE, String.format(format.convertFromSlf4jFormat(), *arguments))
        arguments.printStackTrace()
    }

    private fun String.convertFromSlf4jFormat(): String =
        replace("{}", "%s")

    private fun Array<*>.printStackTrace() {
        filterIsInstance<Throwable>()
            .forEach { it.printStackTrace() }
    }
}

object MockServerChannelManager : McServerChannelManager {
    private val internalHandlers: SetMultimap<String, McServerChannelHandler> =
        Multimaps.newSetMultimap(HashMap(), ::HashSet)

    override val registeredChannels: Collection<String>
        get() = internalHandlers.keys()

    override fun registerChannelHandler(channel: String, handler: McServerChannelHandler) {
        if (internalHandlers.containsKey(channel)) {
            internalHandlers.put(channel, handler)
            return
        } else {
            internalHandlers.put(channel, handler)
        }
    }

    override fun unregisterChannelHandler(channel: String, handler: McServerChannelHandler) {
        internalHandlers.remove(channel, handler)
    }

    override fun clear() {
        internalHandlers.clear()
    }

    fun receivePacket(channelName: String, player: McServerPlayer, message: ByteArray) {
        val handlers = internalHandlers[channelName] ?: return

        handlers.forEach {
            it.receive(player, message)
        }
    }
}

object MockServerLib : McServerLib {
    private val mainExecutor = Executors.newSingleThreadScheduledExecutor()

    override val commandManager = mock<McCommandManager<McCommand>> {
    }

    override val channelManager: McServerChannelManager = MockServerChannelManager

    override val scheduler: McServerScheduler = MainThreadExecutor(mainExecutor)

    override val worlds: Collection<McServerWorld> = listOf(
        mockWorld("world")
    )

    private val playerByName: MutableMap<String, McServerPlayer> = Maps.newConcurrentMap()
    private val playerById: MutableMap<UUID, McServerPlayer> = Maps.newConcurrentMap()

    override val players: Collection<McServerPlayer> = playerById.values

    override val port: Int = 25565
    override val version: String = "1.0.0"

    override val serverTranslator: ServerTranslator = mock<ServerTranslator>()
    override val textConverter: ServerTextConverter<*> = mock<ServerTextConverter<*>>()
    override val permissionManager: PermissionManager = PermissionManager()
    override val configsFolder: File = File("./run")

    init {
        McLoggerFactory.supplier = object : McLoggerFactory.Supplier {
            override fun createLogger(name: String): McLogger = JavaLogger(name)
        }
    }

    override fun executeInMainThread(runnable: Runnable) {
        scheduler.runTask { runnable.run() }
    }

    override fun getWorld(instance: Any): McServerWorld {
        throw NotImplementedError()
    }

    override fun getPlayerByInstance(instance: Any): McServerPlayer =
        instance as McServerPlayer

    override fun getPlayerByName(playerName: String): McServerPlayer? =
        playerByName[playerName]

    override fun getPlayerById(playerId: UUID): McServerPlayer? =
        playerById[playerId]

    override fun getGameProfile(playerId: UUID): McGameProfile? =
        McGameProfile(playerId, playerId.toString(), emptyList())

    override fun getGameProfile(name: String): McGameProfile? =
        McGameProfile(UUID.randomUUID(), name, emptyList())

    override fun getEntityByInstance(instance: Any): McServerEntity {
        throw NotImplementedError()
    }

    fun addPlayer(player: McServerPlayer) {
        playerByName[player.name] = player
        playerById[player.uuid] = player
        McPlayerJoinEvent.invoker.onPlayerJoin(player)
    }
}

object MockVoiceServer : BaseVoiceServer(PlatformLoader.CUSTOM) {
    override fun getConfigFolder(): File = minecraftServer.configsFolder.resolve("plasmo-voice")

    override fun getMinecraftServer(): McServerLib = MockServerLib

    init {
        onInitialize()
    }
}
