package twotoz.skittleCities.utils;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

/**
 * Folia-compatible scheduler wrapper using BukkitScheduler
 * BukkitScheduler works on both Spigot/Paper AND Folia (backwards compatible)
 */
public class SchedulerUtil {
    
    /**
     * Run a task globally (works on both Spigot and Folia)
     * Uses BukkitScheduler which is backwards compatible with Folia
     */
    public static BukkitTask runTaskTimer(Plugin plugin, Runnable task, long delay, long period) {
        return plugin.getServer().getScheduler().runTaskTimer(plugin, task, delay, period);
    }
    
    /**
     * Run a task asynchronously (safe on both)
     */
    public static BukkitTask runTaskTimerAsynchronously(Plugin plugin, Runnable task, long delay, long period) {
        return plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, task, delay, period);
    }
}
