package su.plo.voice.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

object CoroutineScopes {
    val DefaultSupervisor = CoroutineScope(SupervisorJob() + Dispatchers.Default)
}