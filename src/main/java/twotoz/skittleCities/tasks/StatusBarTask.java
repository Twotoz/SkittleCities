package twotoz.skittleCities.tasks;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import twotoz.skittleCities.SkittleCities;
import twotoz.skittleCities.data.Region;
import twotoz.skittleCities.utils.MessageUtil;

public class StatusBarTask extends BukkitRunnable {
    private final SkittleCities plugin;
    private String cachedWorldName;

    public StatusBarTask(SkittleCities plugin) {
        this.plugin = plugin;
        this.cachedWorldName = plugin.getConfig().getString("world-name");
    }

    @Override
    public void run() {
        // Cache world name check
        if (cachedWorldName == null) {
            cachedWorldName = plugin.getConfig().getString("world-name");
        }
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            // Only show status bar in the configured world
            if (!player.getWorld().getName().equals(cachedWorldName)) continue;

            StringBuilder status = new StringBuilder();
            
            // Balance (now cached!)
            double balance = plugin.getEconomyManager().getBalance(player.getUniqueId());
            status.append(MessageUtil.colorize("&6$" + String.format("%.2f", balance)));
            
            status.append(MessageUtil.colorize(" &8| "));
            
            // Claim status (now cached!)
            Region region = plugin.getRegionManager().getRegionAt(player.getLocation());
            if (region != null) {
                String ownerName = "Unknown";
                if (region.getOwner() != null) {
                    if (region.getOwner().equals(player.getUniqueId())) {
                        ownerName = "Your claim";
                    } else {
                        ownerName = Bukkit.getOfflinePlayer(region.getOwner()).getName();
                        if (ownerName == null) ownerName = "Unknown";
                        ownerName += "'s claim";
                    }
                } else {
                    ownerName = "Unclaimed";
                }
                
                status.append(MessageUtil.colorize("&7" + ownerName));
                
                // PVP status
                boolean pvp = plugin.getFlagManager().getClaimFlag(region, "pvp");
                status.append(MessageUtil.colorize(" &8| &7PVP: " + (pvp ? "&aON" : "&cOFF")));
            } else {
                status.append(MessageUtil.colorize("&7Wilderness"));
                
                // World PVP status
                boolean worldPvp = plugin.getFlagManager().getWorldFlag("pvp");
                status.append(MessageUtil.colorize(" &8| &7PVP: " + (worldPvp ? "&aON" : "&cOFF")));
            }

            // Send persistent status bar (only if no temporary message)
            plugin.getActionBarManager().sendPersistent(player, status.toString());
        }
    }
}
