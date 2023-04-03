package su.plo.lib.paper.world

import org.bukkit.Bukkit
import org.bukkit.entity.Entity
import org.bukkit.plugin.Plugin


object FoliaUtils {

    /*
     * Schedules a task to run for a given entity.
     *
     * For non-Folia servers, runs on Bukkit scheduler.
     * For Folia servers, runs on the entity's scheduler.
     */

    @Suppress("deprecation")
    fun runTaskFor(entity: Entity, plugin: Plugin?, task: Runnable) {
        if (isFolia()) {
            if (plugin != null) {
                entity.scheduler.run(plugin, { task.run() }, null)
            }
        } else {
            if (plugin != null) {
                Bukkit.getScheduler().runTask(plugin, task)
            }
        }
    }

    private fun isFolia(): Boolean {
        return try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer")
            true
        } catch (e: ClassNotFoundException) {
            false
        }
    }
}
