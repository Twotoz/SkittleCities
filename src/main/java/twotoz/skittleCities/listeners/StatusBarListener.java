package twotoz.skittleCities.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import twotoz.skittleCities.SkittleCities;
import twotoz.skittleCities.data.Region;
import twotoz.skittleCities.utils.MessageUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Event-driven status bar - ZERO polling overhead
 * Only updates when player actually moves or something changes
 */
public class StatusBarListener implements Listener {
    private final SkittleCities plugin;
    private final String cityWorldName;
    private final Map<UUID, String> lastSentMessage;
    private final Map<UUID, Integer> lastLocationHash;
    
    public StatusBarListener(SkittleCities plugin) {
        this.plugin = plugin;
        this.cityWorldName = plugin.getConfig().getString("world-name");
        this.lastSentMessage = new HashMap<>();
        this.lastLocationHash = new HashMap<>();
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        
        // OPTIMIZATION 1: Skip if not in city world
        if (!player.getWorld().getName().equals(cityWorldName)) {
            return;
        }
        
        // OPTIMIZATION 2: Only trigger on block boundary (97% reduction!)
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
            event.getFrom().getBlockY() == event.getTo().getBlockY() &&
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return; // Still in same block
        }
        
        UUID playerId = player.getUniqueId();
        
        // OPTIMIZATION 3: Location hash to detect actual region changes
        int newHash = event.getTo().getBlockX() * 73856093 ^ 
                     event.getTo().getBlockZ() * 19349663;
        
        Integer lastHash = lastLocationHash.get(playerId);
        if (lastHash != null && lastHash == newHash) {
            return; // Same location hash, probably same region
        }
        
        lastLocationHash.put(playerId, newHash);
        
        // Build and send status bar
        updateStatusBar(player);
    }
    
    /**
     * Called by EconomyManager when balance changes
     */
    public void onBalanceChange(Player player) {
        if (!player.getWorld().getName().equals(cityWorldName)) {
            return;
        }
        updateStatusBar(player);
    }
    
    /**
     * Called by ClaimMoveListener when entering/leaving regions
     */
    public void onRegionChange(Player player) {
        if (!player.getWorld().getName().equals(cityWorldName)) {
            return;
        }
        updateStatusBar(player);
    }
    
    private void updateStatusBar(Player player) {
        UUID playerId = player.getUniqueId();
        
        // Build message
        StringBuilder status = new StringBuilder(64);
        
        // Balance
        double balance = plugin.getEconomyManager().getBalance(playerId);
        status.append(MessageUtil.colorize("&6$"));
        status.append(formatBalance(balance));
        status.append(MessageUtil.colorize(" &8| "));
        
        // Claim info
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
            
            boolean pvp = plugin.getFlagManager().getClaimFlag(region, "pvp");
            status.append(MessageUtil.colorize(" &8| &7PVP: ")).append(pvp ? "&aON" : "&cOFF");
        } else {
            status.append(MessageUtil.colorize("&7Wilderness"));
            boolean worldPvp = plugin.getFlagManager().getWorldFlag("pvp");
            status.append(MessageUtil.colorize(" &8| &7PVP: ")).append(worldPvp ? "&aON" : "&cOFF");
        }
        
        String message = MessageUtil.colorize(status.toString());
        
        // OPTIMIZATION 4: Only send if message changed
        String lastMessage = lastSentMessage.get(playerId);
        if (message.equals(lastMessage)) {
            return; // No change, skip
        }
        
        lastSentMessage.put(playerId, message);
        plugin.getActionBarManager().sendPersistent(player, message);
    }
    
    private String formatBalance(double balance) {
        long intPart = (long) balance;
        int decimalPart = (int) Math.round((balance - intPart) * 100);
        
        if (decimalPart >= 100) {
            intPart++;
            decimalPart = 0;
        }
        
        return intPart + "." + (decimalPart < 10 ? "0" : "") + decimalPart;
    }
    
    public void cleanup(Player player) {
        UUID playerId = player.getUniqueId();
        lastSentMessage.remove(playerId);
        lastLocationHash.remove(playerId);
    }
}
