package twotoz.skittleCities.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import twotoz.skittleCities.SkittleCities;

public class PlayerJoinListener implements Listener {
    private final SkittleCities plugin;

    public PlayerJoinListener(SkittleCities plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Preload balance into cache
        plugin.getEconomyManager().preloadBalance(player.getUniqueId());
        
        // Give default balance if first join
        double currentBalance = plugin.getEconomyManager().getBalance(player.getUniqueId());
        double defaultBalance = plugin.getConfig().getDouble("default-balance", 1000);
        
        // If balance is exactly the default, they likely never got a balance set
        // So we check if they've played before
        if (currentBalance == defaultBalance && !player.hasPlayedBefore()) {
            plugin.getEconomyManager().setBalance(player.getUniqueId(), defaultBalance);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Cleanup
        plugin.getSelectionManager().removeSelection(event.getPlayer());
        plugin.getDebugToolCommand().cleanup(event.getPlayer());
        plugin.getIgnoreClaimsCommand().cleanup(event.getPlayer());
        plugin.getClaimMoveListener().cleanup(event.getPlayer());
    }
}
