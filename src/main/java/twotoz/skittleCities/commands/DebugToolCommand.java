package twotoz.skittleCities.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import twotoz.skittleCities.SkittleCities;
import twotoz.skittleCities.data.Region;
import twotoz.skittleCities.utils.MessageUtil;
import twotoz.skittleCities.utils.ParticleUtil;
import twotoz.skittleCities.utils.SchedulerUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DebugToolCommand implements CommandExecutor {
    private final SkittleCities plugin;
    private final Map<UUID, BukkitTask> activeTasks;

    public DebugToolCommand(SkittleCities plugin) {
        this.plugin = plugin;
        this.activeTasks = new HashMap<>();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtil.send(sender, plugin.getConfig(), "player-only");
            return true;
        }

        if (!player.hasPermission("skittlecities.debug")) {
            MessageUtil.send(player, plugin.getConfig(), "no-permission");
            return true;
        }

        UUID playerId = player.getUniqueId();

        if (activeTasks.containsKey(playerId)) {
            // Disable
            activeTasks.get(playerId).cancel();
            activeTasks.remove(playerId);
            MessageUtil.send(player, plugin.getConfig(), "debug-tool-disabled");
        } else {
            // Enable - Folia compatible
            Runnable task = () -> {
                for (Region region : plugin.getRegionManager().getAllRegions()) {
                    if (region.getWorld().equals(player.getWorld())) {
                        ParticleUtil.visualizeRegion(player, region);
                    }
                }
            };
            
            BukkitTask scheduledTask = SchedulerUtil.runTaskTimer(plugin, task, 0L, 20L);
            activeTasks.put(playerId, scheduledTask);
            MessageUtil.send(player, plugin.getConfig(), "debug-tool-enabled");
        }

        return true;
    }

    public void cleanup(Player player) {
        UUID playerId = player.getUniqueId();
        if (activeTasks.containsKey(playerId)) {
            activeTasks.get(playerId).cancel();
            activeTasks.remove(playerId);
        }
    }
}
