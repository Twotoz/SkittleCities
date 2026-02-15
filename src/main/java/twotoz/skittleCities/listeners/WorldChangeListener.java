package twotoz.skittleCities.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import twotoz.skittleCities.SkittleCities;

public class WorldChangeListener implements Listener {
    private final SkittleCities plugin;

    public WorldChangeListener(SkittleCities plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        String cityWorld = plugin.getConfig().getString("world-name");
        String fromWorld = event.getFrom().getName();
        String toWorld = player.getWorld().getName();

        // Check if switching between city and other worlds
        boolean wasInCity = fromWorld.equals(cityWorld);
        boolean isInCity = toWorld.equals(cityWorld);

        if (!wasInCity && isInCity) {
            // Entering city world
            plugin.getInventoryManager().switchToCityInventory(player);
            player.sendMessage(org.bukkit.ChatColor.GREEN + "Your inventory has been switched to city mode!");
        } else if (wasInCity && !isInCity) {
            // Leaving city world
            plugin.getInventoryManager().switchToOtherWorldInventory(player);
            player.sendMessage(org.bukkit.ChatColor.GREEN + "Your inventory has been restored!");
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Load correct inventory on join based on which world they're in
        if (plugin.getInventoryManager().isInCityWorld(player)) {
            // Player joined in city world - inventory is already correct from login
            // But we need to mark it as city inventory for next world change
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // Save current inventory before quit
        plugin.getInventoryManager().saveInventoryOnQuit(player);
    }
}
