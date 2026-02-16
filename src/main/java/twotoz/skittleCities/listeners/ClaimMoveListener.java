package twotoz.skittleCities.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import twotoz.skittleCities.SkittleCities;
import twotoz.skittleCities.data.Region;
import twotoz.skittleCities.utils.MessageUtil;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ClaimMoveListener implements Listener {
    private final SkittleCities plugin;
    // Thread-safe map to prevent race conditions
    private final ConcurrentHashMap<UUID, Region> playerLastRegion;

    public ClaimMoveListener(SkittleCities plugin) {
        this.plugin = plugin;
        this.playerLastRegion = new ConcurrentHashMap<>();
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        
        // Only check if player crossed block boundary
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
            event.getFrom().getBlockY() == event.getTo().getBlockY() &&
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        // Check if in configured world
        String worldName = plugin.getConfig().getString("world-name");
        if (!player.getWorld().getName().equals(worldName)) return;

        Region currentRegion = plugin.getRegionManager().getRegionAt(event.getTo());
        Region lastRegion = playerLastRegion.get(player.getUniqueId());

        // Check if region changed
        if (currentRegion != lastRegion) {
            // Check if this is a parent<->subclaim transition
            boolean isParentSubclaimTransition = isParentSubclaimTransition(lastRegion, currentRegion);
            boolean sameOwner = haveSameOwner(lastRegion, currentRegion);
            boolean importantFlagsChanged = importantFlagsChanged(lastRegion, currentRegion);
            
            if (currentRegion == null && lastRegion != null) {
                // Left a claim to wilderness
                handleClaimLeave(player, lastRegion);
            } else if (currentRegion != null && lastRegion == null) {
                // Entered a claim from wilderness
                handleClaimEnter(player, currentRegion);
            } else if (currentRegion != null && lastRegion != null) {
                // Moved from one claim to another
                // SKIP messages if: parent<->subclaim transition + same owner + no important flag changes
                if (isParentSubclaimTransition && sameOwner && !importantFlagsChanged) {
                    // Silent transition - just update status bar
                    if (plugin.getStatusBarListener() != null) {
                        plugin.getStatusBarListener().onRegionChange(player);
                    }
                } else {
                    // Show messages (different claims or important changes)
                    handleClaimLeave(player, lastRegion);
                    handleClaimEnter(player, currentRegion);
                }
            }

            // Thread-safe update
            playerLastRegion.put(player.getUniqueId(), currentRegion);
        }
    }
    
    /**
     * Check if transition is between parent and subclaim
     */
    private boolean isParentSubclaimTransition(Region region1, Region region2) {
        if (region1 == null || region2 == null) return false;
        
        // Check if region1 is subclaim of region2
        if (region1.isSubclaim() && region1.getParentId() != null && region1.getParentId() == region2.getId()) {
            return true;
        }
        
        // Check if region2 is subclaim of region1
        if (region2.isSubclaim() && region2.getParentId() != null && region2.getParentId() == region1.getId()) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Check if two regions have the same owner
     */
    private boolean haveSameOwner(Region region1, Region region2) {
        if (region1 == null || region2 == null) return false;
        
        UUID owner1 = region1.getOwner();
        UUID owner2 = region2.getOwner();
        
        // Both must have owners and they must be equal
        return owner1 != null && owner2 != null && owner1.equals(owner2);
    }
    
    /**
     * Check if important flags changed between regions
     * Important flags: pvp, fly
     */
    private boolean importantFlagsChanged(Region region1, Region region2) {
        if (region1 == null || region2 == null) return true; // Consider as changed if one is null
        
        // Check PVP flag (most important)
        boolean pvp1 = plugin.getFlagManager().getClaimFlag(region1, "pvp");
        boolean pvp2 = plugin.getFlagManager().getClaimFlag(region2, "pvp");
        if (pvp1 != pvp2) return true;
        
        // Check FLY flag
        boolean fly1 = plugin.getFlagManager().getClaimFlag(region1, "fly");
        boolean fly2 = plugin.getFlagManager().getClaimFlag(region2, "fly");
        if (fly1 != fly2) return true;
        
        // No important changes
        return false;
    }

    private void handleClaimEnter(Player player, Region region) {
        // Update status bar
        if (plugin.getStatusBarListener() != null) {
            plugin.getStatusBarListener().onRegionChange(player);
        }
        
        // Special message for safezone
        if (region.getType() == Region.RegionType.SAFEZONE) {
            String message = MessageUtil.colorize("&aEntered safezone");
            plugin.getActionBarManager().sendTemporary(player, message);
            return;
        }

        // Get display name (uses parent name for subclaims)
        String displayName;
        if (region.isSubclaim()) {
            Region parent = plugin.getRegionManager().getParentClaim(region);
            displayName = parent != null && parent.getDisplayName() != null ? 
                parent.getDisplayName() : (parent != null ? parent.getName() : "claim");
        } else {
            displayName = region.getDisplayName() != null ? region.getDisplayName() : region.getName();
        }
        
        String ownerName = "Unknown";
        if (region.getOwner() != null) {
            ownerName = plugin.getServer().getOfflinePlayer(region.getOwner()).getName();
            if (ownerName == null) ownerName = "Unknown";
        } else {
            ownerName = "Unclaimed";
        }

        boolean pvp = plugin.getFlagManager().getClaimFlag(region, "pvp");
        
        String message = MessageUtil.colorize("&7Entered &e" + ownerName + "'s claim &8| &7PVP: " + 
            (pvp ? "&aON" : "&cOFF"));
        
        plugin.getActionBarManager().sendTemporary(player, message);
    }

    private void handleClaimLeave(Player player, Region region) {
        // Update status bar
        if (plugin.getStatusBarListener() != null) {
            plugin.getStatusBarListener().onRegionChange(player);
        }
        
        // Special message for leaving safezone
        if (region.getType() == Region.RegionType.SAFEZONE) {
            boolean worldPvp = plugin.getFlagManager().getWorldFlag("pvp");
            String message = MessageUtil.colorize("&7Left safezone &8| &7PVP: " + 
                (worldPvp ? "&aON" : "&cOFF"));
            plugin.getActionBarManager().sendTemporary(player, message);
            return;
        }

        boolean worldPvp = plugin.getFlagManager().getWorldFlag("pvp");
        
        String message = MessageUtil.colorize("&7Left claim &8| &7PVP: " + 
            (worldPvp ? "&aON" : "&cOFF"));
        
        plugin.getActionBarManager().sendTemporary(player, message);
    }

    /**
     * Thread-safe cleanup
     */
    public void cleanup(Player player) {
        playerLastRegion.remove(player.getUniqueId());
    }
}
