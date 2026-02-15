package twotoz.skittleCities.tasks;

import org.bukkit.entity.Player;
import twotoz.skittleCities.SkittleCities;
import twotoz.skittleCities.data.Region;
import twotoz.skittleCities.utils.MessageUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StatusBarTask implements Runnable {
    private final SkittleCities plugin;
    private String cachedWorldName;
    private final Map<UUID, String> cachedStatusMessages;
    private final Map<UUID, Long> lastBalanceCheck;
    private final Map<UUID, Integer> lastLocationHash;

    public StatusBarTask(SkittleCities plugin) {
        this.plugin = plugin;
        this.cachedWorldName = plugin.getConfig().getString("world-name");
        this.cachedStatusMessages = new HashMap<>();
        this.lastBalanceCheck = new HashMap<>();
        this.lastLocationHash = new HashMap<>();
    }

    @Override
    public void run() {
        // Cache world name check
        if (cachedWorldName == null) {
            cachedWorldName = plugin.getConfig().getString("world-name");
        }
        
        long currentTime = System.currentTimeMillis();
        
        // FOLIA COMPATIBLE: Use Server.getOnlinePlayers() instead of Bukkit
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            // Only show status bar in the configured world
            if (!player.getWorld().getName().equals(cachedWorldName)) continue;

            UUID playerId = player.getUniqueId();
            
            // Create a simple location hash to detect movement
            int locationHash = player.getLocation().getBlockX() * 73856093 ^ 
                              player.getLocation().getBlockZ() * 19349663;
            
            Integer lastHash = lastLocationHash.get(playerId);
            boolean playerMoved = lastHash == null || lastHash != locationHash;
            
            // Only check balance every 5 seconds to reduce cache lookups
            Long lastCheck = lastBalanceCheck.get(playerId);
            boolean shouldUpdateBalance = lastCheck == null || currentTime - lastCheck > 5000;
            
            // Check if we need to rebuild the status message
            String cachedMessage = cachedStatusMessages.get(playerId);
            if (cachedMessage == null || playerMoved || shouldUpdateBalance) {
                StringBuilder status = new StringBuilder(64); // Pre-allocate reasonable size
                
                // Balance - use simple formatting instead of String.format
                if (shouldUpdateBalance) {
                    double balance = plugin.getEconomyManager().getBalance(playerId);
                    status.append(MessageUtil.colorize("&6$"));
                    status.append(formatBalance(balance));
                    lastBalanceCheck.put(playerId, currentTime);
                } else {
                    // Reuse the balance part from cached message
                    if (cachedMessage != null) {
                        int pipeIndex = cachedMessage.indexOf('|');
                        if (pipeIndex > 0) {
                            status.append(cachedMessage, 0, pipeIndex);
                        }
                    } else {
                        double balance = plugin.getEconomyManager().getBalance(playerId);
                        status.append(MessageUtil.colorize("&6$"));
                        status.append(formatBalance(balance));
                        lastBalanceCheck.put(playerId, currentTime);
                    }
                }
                
                status.append(MessageUtil.colorize(" &8| "));
                
                // Claim status (cached via RegionCache)
                Region region = plugin.getRegionManager().getRegionAt(player.getLocation());
                if (region != null) {
                    String ownerName;
                    if (region.getOwner() != null) {
                        if (region.getOwner().equals(playerId)) {
                            ownerName = "Your claim";
                        } else {
                            ownerName = plugin.getServer().getOfflinePlayer(region.getOwner()).getName();
                            if (ownerName == null) ownerName = "Unknown";
                            ownerName += "'s claim";
                        }
                    } else {
                        ownerName = "Unclaimed";
                    }
                    
                    status.append(MessageUtil.colorize("&7")).append(ownerName);
                    
                    // PVP status
                    boolean pvp = plugin.getFlagManager().getClaimFlag(region, "pvp");
                    status.append(MessageUtil.colorize(" &8| &7PVP: ")).append(pvp ? "&aON" : "&cOFF");
                } else {
                    status.append(MessageUtil.colorize("&7Wilderness"));
                    
                    // World PVP status
                    boolean worldPvp = plugin.getFlagManager().getWorldFlag("pvp");
                    status.append(MessageUtil.colorize(" &8| &7PVP: ")).append(worldPvp ? "&aON" : "&cOFF");
                }

                // Colorize the final string once
                String finalMessage = MessageUtil.colorize(status.toString());
                cachedStatusMessages.put(playerId, finalMessage);
                lastLocationHash.put(playerId, locationHash);
                
                // Send persistent status bar
                plugin.getActionBarManager().sendPersistent(player, finalMessage);
            } else {
                // Use cached message
                plugin.getActionBarManager().sendPersistent(player, cachedMessage);
            }
        }
    }

    /**
     * Fast balance formatter - avoids expensive String.format
     */
    private String formatBalance(double balance) {
        long intPart = (long) balance;
        int decimalPart = (int) Math.round((balance - intPart) * 100);
        
        if (decimalPart >= 100) {
            intPart++;
            decimalPart = 0;
        }
        
        StringBuilder sb = new StringBuilder(16);
        sb.append(intPart);
        sb.append('.');
        
        if (decimalPart < 10) {
            sb.append('0');
        }
        sb.append(decimalPart);
        
        return sb.toString();
    }
}
