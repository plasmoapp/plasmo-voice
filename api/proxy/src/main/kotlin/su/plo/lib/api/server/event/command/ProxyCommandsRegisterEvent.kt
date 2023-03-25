package su.plo.lib.api.server.event.command

import com.google.inject.Inject
import su.plo.lib.api.proxy.MinecraftProxyLib
import su.plo.lib.api.proxy.command.MinecraftProxyCommand

/**
 * This event is fired BEFORE Plasmo Voice addons initialization.
 * Fields annotated with [Inject] will NOT be injected yet
 */
object ProxyCommandsRegisterEvent : CommandsRegisterEvent<MinecraftProxyCommand, MinecraftProxyLib>()
