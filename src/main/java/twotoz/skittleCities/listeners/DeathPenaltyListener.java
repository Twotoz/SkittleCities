package twotoz.skittleCities.listeners;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import twotoz.skittleCities.SkittleCities;
import twotoz.skittleCities.utils.MessageUtil;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class DeathPenaltyListener implements Listener {
    private final SkittleCities plugin;
    // Track players that died in city so we redirect their respawn
    private final Set<UUID> diedInCity = new HashSet<>();

    public DeathPenaltyListener(SkittleCities plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        String cityWorld = plugin.getConfig().getString("world-name");

        if (!player.getWorld().getName().equalsIgnoreCase(cityWorld)) return;

        diedInCity.add(player.getUniqueId());

        // 10% balance penalty
        double balance = plugin.getEconomyManager().getBalance(player.getUniqueId());

        if (balance > 0) {
            // Race condition safety: use exact current balance, floor to 2 decimals
            double penalty = Math.floor(balance * 0.10 * 100) / 100.0;
            // Clamp: never take more than they have
            penalty = Math.min(penalty, balance);

            if (penalty > 0) {
                plugin.getEconomyManager().removeBalance(player.getUniqueId(), penalty);

                // Notify in death message area (we can't use action bar since they're dead)
                String msg = MessageUtil.colorize(
                    plugin.getConfig().getString("messages.prefix") +
                    "&cYou lost &6$" + String.format("%.2f", penalty) +
                    " &c(10% death penalty). Remaining: &6$" +
                    String.format("%.2f", balance - penalty));

                // Schedule for after respawn (player isn't fully initialized yet)
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    if (player.isOnline()) {
                        plugin.getActionBarManager().sendTemporary(player, msg);
                    }
                }, 5L);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        if (!diedInCity.remove(player.getUniqueId())) return;

        // Check if hospital spawn has been explicitly configured
        String section = plugin.getConfig().getBoolean("hospital-spawn.configured", false)
            ? "hospital-spawn"
            : "city-spawn";

        String worldName = plugin.getConfig().getString(section + ".world",
            plugin.getConfig().getString("world-name"));

        World world = plugin.getServer().getWorld(worldName);
        if (world == null) return;

        double x = plugin.getConfig().getDouble(section + ".x");
        double y = plugin.getConfig().getDouble(section + ".y");
        double z = plugin.getConfig().getDouble(section + ".z");
        float yaw = (float) plugin.getConfig().getDouble(section + ".yaw");
        float pitch = (float) plugin.getConfig().getDouble(section + ".pitch");

        event.setRespawnLocation(new Location(world, x, y, z, yaw, pitch));
    }
}
