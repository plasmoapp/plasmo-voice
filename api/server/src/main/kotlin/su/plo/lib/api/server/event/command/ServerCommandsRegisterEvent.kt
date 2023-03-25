package su.plo.lib.api.server.event.command

import com.google.inject.Inject
import su.plo.lib.api.server.MinecraftServerLib
import su.plo.lib.api.server.command.MinecraftCommand

/**
 * This event is fired BEFORE Plasmo Voice addons initialization.
 * Fields annotated with [Inject] will NOT be injected yet
 */
object ServerCommandsRegisterEvent : CommandsRegisterEvent<MinecraftCommand, MinecraftServerLib>()
