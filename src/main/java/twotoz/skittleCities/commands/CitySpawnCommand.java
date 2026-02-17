package twotoz.skittleCities.commands;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import twotoz.skittleCities.SkittleCities;
import twotoz.skittleCities.utils.MessageUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CitySpawnCommand implements CommandExecutor {
    private final SkittleCities plugin;
    private final Map<UUID, org.bukkit.scheduler.BukkitTask> pendingTeleports = new HashMap<>();

    public CitySpawnCommand(SkittleCities plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Player only.");
            return true;
        }

        String cityWorld = plugin.getConfig().getString("world-name");

        // Must be in city world
        if (!player.getWorld().getName().equalsIgnoreCase(cityWorld)) {
            plugin.getActionBarManager().sendTemporary(player,
                MessageUtil.colorize("&cYou must be in the city to use /cspawn!"));
            return true;
        }

        // Already at spawn area? Still allow it (no pointless check)

        // Combat check
        if (plugin.getCombatManager().isInCombat(player.getUniqueId())) {
            int remaining = plugin.getCombatManager().getRemainingCombatTime(player.getUniqueId());
            plugin.getActionBarManager().sendTemporary(player,
                MessageUtil.colorize("&cIn combat! Wait &e" + remaining + "s &cbefore teleporting."));
            return true;
        }

        // Cancel any existing pending teleport
        if (pendingTeleports.containsKey(player.getUniqueId())) {
            pendingTeleports.get(player.getUniqueId()).cancel();
        }

        // 3 second countdown
        plugin.getActionBarManager().sendTemporary(player,
            MessageUtil.colorize("&eTeleporting to spawn in &63s&e..."));

        final Location startLocation = player.getLocation().clone();

        org.bukkit.scheduler.BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            pendingTeleports.remove(player.getUniqueId());

            if (!player.isOnline()) return;

            if (plugin.getCombatManager().isInCombat(player.getUniqueId())) {
                plugin.getActionBarManager().sendTemporary(player,
                    MessageUtil.colorize("&cTeleport cancelled! You entered combat."));
                return;
            }

            if (player.getLocation().distance(startLocation) > 1.0) {
                plugin.getActionBarManager().sendTemporary(player,
                    MessageUtil.colorize("&cTeleport cancelled! Don't move."));
                return;
            }

            World world = plugin.getServer().getWorld(cityWorld);
            if (world == null) return;

            double x = plugin.getConfig().getDouble("city-spawn.x");
            double y = plugin.getConfig().getDouble("city-spawn.y");
            double z = plugin.getConfig().getDouble("city-spawn.z");
            float yaw = (float) plugin.getConfig().getDouble("city-spawn.yaw");
            float pitch = (float) plugin.getConfig().getDouble("city-spawn.pitch");

            player.teleport(new Location(world, x, y, z, yaw, pitch));
            plugin.getActionBarManager().sendTemporary(player,
                MessageUtil.colorize("&aTeleported to city spawn!"));

        }, 20L * 3);

        pendingTeleports.put(player.getUniqueId(), task);
        return true;
    }

    public void cancelPending(UUID playerId) {
        org.bukkit.scheduler.BukkitTask task = pendingTeleports.remove(playerId);
        if (task != null) task.cancel();
    }
}
