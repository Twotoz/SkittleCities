package twotoz.skittleCities.listeners;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import twotoz.skittleCities.SkittleCities;
import twotoz.skittleCities.utils.MessageUtil;

public class FlyBlockListener implements Listener {
    private final SkittleCities plugin;

    public FlyBlockListener(SkittleCities plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Start periodic fly check task (runs every 30 seconds)
     */
    public void startPeriodicCheck() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            checkAllPlayers();
        }, 20L * 30, 20L * 30); // 30 seconds in ticks
    }
    
    /**
     * Check all online players for illegal flying
     */
    private void checkAllPlayers() {
        String cityWorld = plugin.getConfig().getString("world-name", "city");
        
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            // Not in city world - skip
            if (!player.getWorld().getName().equals(cityWorld)) {
                continue;
            }
            
            // Not flying - skip
            if (!player.isFlying() && !player.getAllowFlight()) {
                continue;
            }
            
            // Creative/Spectator mode - allow
            if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
                continue;
            }
            
            // Has bypass permission - allow
            if (player.hasPermission("skittlecities.fly")) {
                continue;
            }
            
            // ILLEGAL FLYING - disable
            player.setAllowFlight(false);
            player.setFlying(false);
            player.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
                "&cFlying is not allowed in the city!"));
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Disable fly on join if in city world
        checkAndDisableFly(player);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        
        // Check fly when changing worlds
        checkAndDisableFly(player);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        
        // Only check if player is trying to ENABLE flight
        if (!event.isFlying()) {
            return;
        }
        
        String cityWorld = plugin.getConfig().getString("world-name", "city");
        
        // Not in city world - allow
        if (!player.getWorld().getName().equals(cityWorld)) {
            return;
        }
        
        // Creative/Spectator mode - always allow
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }
        
        // Check bypass permission
        if (player.hasPermission("skittlecities.fly")) {
            return; // Has bypass - allow
        }
        
        // BLOCK flight
        event.setCancelled(true);
        player.setAllowFlight(false);
        player.setFlying(false);
        
        player.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
            "&cFlying is not allowed in the city!"));
    }

    /**
     * Check and disable fly if player shouldn't be flying
     */
    private void checkAndDisableFly(Player player) {
        String cityWorld = plugin.getConfig().getString("world-name", "city");
        
        // Not in city world - don't touch
        if (!player.getWorld().getName().equals(cityWorld)) {
            return;
        }
        
        // Creative/Spectator mode - allow
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }
        
        // Has bypass permission - allow
        if (player.hasPermission("skittlecities.fly")) {
            return;
        }
        
        // Disable fly
        if (player.isFlying() || player.getAllowFlight()) {
            player.setAllowFlight(false);
            player.setFlying(false);
            
            player.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
                "&cFlying is disabled in the city!"));
        }
    }
}
