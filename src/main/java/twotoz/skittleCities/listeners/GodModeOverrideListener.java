package twotoz.skittleCities.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import twotoz.skittleCities.SkittleCities;

/**
 * Ensures only SkittleCities controls damage and invulnerability in city world.
 * Overrides god mode plugins, invulnerability flags, etc.
 * Does NOTHING outside city world.
 */
public class GodModeOverrideListener implements Listener {
    private final SkittleCities plugin;
    
    public GodModeOverrideListener(SkittleCities plugin) {
        this.plugin = plugin;
    }
    
    private boolean isInCityWorld(Player player) {
        String cityWorld = plugin.getConfig().getString("world-name");
        return player.getWorld().getName().equalsIgnoreCase(cityWorld);
    }
    
    private boolean hasBypass(Player player) {
        return player.hasPermission("skittlecities.godmode");
    }
    
    /**
     * Remove invulnerability flag when entering city world.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        if (!isInCityWorld(player)) return;
        if (hasBypass(player)) return;
        
        if (player.isInvulnerable()) {
            player.setInvulnerable(false);
            player.sendMessage(org.bukkit.ChatColor.RED + "God mode is disabled in the city.");
        }
    }
    
    /**
     * Remove invulnerability flag when joining in city world.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!isInCityWorld(player)) return;
        if (hasBypass(player)) return;
        
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.isInvulnerable()) {
                player.setInvulnerable(false);
                player.sendMessage(org.bukkit.ChatColor.RED + "God mode is disabled in the city.");
            }
        }, 1L);
    }
    
    /**
     * Override any cancelled damage events in city world.
     * Runs at MONITOR priority = LAST, after all other plugins.
     * ignoreCancelled = false so we can see what other plugins cancelled.
     * 
     * Outside city world: do nothing - other plugins handle it.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!isInCityWorld(player)) return;
        if (hasBypass(player)) return;
        
        // Another plugin cancelled this damage - override it
        if (event.isCancelled()) {
            event.setCancelled(false);
        }
        
        // Also ensure invulnerability is off in case it was set mid-session
        if (player.isInvulnerable()) {
            player.setInvulnerable(false);
        }
    }
    
    /**
     * Override cancelled attack events (PVP) in city world.
     * Separate handler for EntityDamageByEntityEvent (player attacks player).
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // Only handle when attacker is a player
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!isInCityWorld(attacker)) return;
        if (hasBypass(attacker)) return;
        
        // Target has invulnerability - remove it and let damage through
        if (event.getEntity() instanceof Player target && target.isInvulnerable()) {
            target.setInvulnerable(false);
        }
    }
}
