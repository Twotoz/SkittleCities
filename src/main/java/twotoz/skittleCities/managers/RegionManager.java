package twotoz.skittleCities.managers;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import twotoz.skittleCities.SkittleCities;
import twotoz.skittleCities.data.Region;

import java.util.*;

public class RegionManager {
    private final SkittleCities plugin;
    private final List<Region> regions;
    private final RegionCache regionCache;

    public RegionManager(SkittleCities plugin) {
        this.plugin = plugin;
        this.regions = new ArrayList<>();
        this.regionCache = new RegionCache();
    }

    public void loadRegions() {
        regions.clear();
        regions.addAll(plugin.getDatabaseManager().loadAllRegions());
        regionCache.setRegions(regions);
    }

    public void createRegion(Region region) {
        regions.add(region);
        plugin.getDatabaseManager().saveRegion(region);
        regionCache.setRegions(regions);
        
        // Create sign if needed (not for PRIVATE or SAFEZONE)
        if (region.getType() != Region.RegionType.PRIVATE && 
            region.getType() != Region.RegionType.SAFEZONE && 
            region.getSignLocation() != null) {
            createRegionSign(region);
        }
    }

    public void updateRegion(Region region) {
        plugin.getDatabaseManager().updateRegion(region);
        regionCache.invalidateAll(); // Invalidate cache when region updates
    }

    public Region getRegionAt(Location location) {
        return regionCache.getRegionAt(location);
    }

    public List<Region> getAllRegions() {
        return regionCache.getAllRegions();
    }

    public boolean hasOverlap(Region newRegion) {
        for (Region existing : regions) {
            // Skip if same region
            if (existing.getId() == newRegion.getId()) continue;
            
            // Allow if new region is subclaim of existing (completely inside)
            boolean isSubclaimOfExisting = isCompletelyInside(newRegion, existing);
            
            // Allow if existing is subclaim of new region (new is parent)
            boolean existingIsSubclaimOfNew = isCompletelyInside(existing, newRegion);
            
            // Allow if both are subclaims of same parent (volume priority will handle it)
            if (newRegion.getParentId() != null && existing.getParentId() != null && 
                newRegion.getParentId().equals(existing.getParentId())) {
                continue; // Both are subclaims of same parent - OK
            }
            
            // Allow if it's a subclaim/parent relationship
            if (isSubclaimOfExisting || existingIsSubclaimOfNew) {
                continue; // This is OK - it's a subclaim/parent relationship
            }
            
            // Check for partial overlap (not allowed)
            if (existing.overlaps(newRegion)) {
                return true; // Partial overlap = BAD
            }
        }
        return false;
    }
    
    /**
     * Check if region A is completely inside region B
     */
    private boolean isCompletelyInside(Region inner, Region outer) {
        return inner.getMinX() >= outer.getMinX() &&
               inner.getMaxX() <= outer.getMaxX() &&
               inner.getMinY() >= outer.getMinY() &&
               inner.getMaxY() <= outer.getMaxY() &&
               inner.getMinZ() >= outer.getMinZ() &&
               inner.getMaxZ() <= outer.getMaxZ();
    }

    public void handleLeaseExpiry(Region region) {
        // Reset ownership
        region.setOwner(null);
        region.setLeaseExpiry(0);
        
        // Clear trusted players
        region.getTrustedPlayers().clear();
        
        // Update database
        updateRegion(region);
        
        // Recreate sign
        recreateSign(region);
    }

    public void handleLeaseRenewal(Region region) {
        // Extend lease by the configured days
        long currentExpiry = region.getLeaseExpiry();
        long extension = region.getLeaseDays() * 24L * 60L * 60L * 1000L;
        region.setLeaseExpiry(currentExpiry + extension);
        
        // Update database
        updateRegion(region);
    }

    public void recreateSign(Region region) {
        if (region.getSignLocation() == null) return;
        if (region.getType() == Region.RegionType.PRIVATE || 
            region.getType() == Region.RegionType.SAFEZONE) return;
        
        // CRITICAL: Don't create sign if claim is owned
        if (region.getOwner() != null) return;
        
        createRegionSign(region);
    }

    public void deleteRegion(Region region) {
        // CASCADE DELETE: Remove all subclaims first
        List<Region> subclaims = getSubclaims(region.getId());
        for (Region subclaim : subclaims) {
            regions.remove(subclaim);
            plugin.getDatabaseManager().deleteRegion(subclaim.getId());
        }
        
        // Remove the region itself
        regions.remove(region);
        plugin.getDatabaseManager().deleteRegion(region.getId());
        regionCache.setRegions(regions);
        
        // Remove sign if exists
        if (region.getSignLocation() != null) {
            region.getSignLocation().getBlock().setType(org.bukkit.Material.AIR);
        }
    }
    
    /**
     * Get all subclaims of a parent claim
     */
    public List<Region> getSubclaims(int parentId) {
        List<Region> subclaims = new ArrayList<>();
        for (Region region : regions) {
            if (region.getParentId() != null && region.getParentId() == parentId) {
                subclaims.add(region);
            }
        }
        return subclaims;
    }
    
    /**
     * Get parent claim of a subclaim
     */
    public Region getParentClaim(Region subclaim) {
        if (subclaim.getParentId() == null) return null;
        
        for (Region region : regions) {
            if (region.getId() == subclaim.getParentId()) {
                return region;
            }
        }
        return null;
    }

    private void createRegionSign(Region region) {
        Location signLoc = region.getSignLocation();
        Block block = signLoc.getBlock();
        
        // Set block to wall sign
        block.setType(org.bukkit.Material.OAK_WALL_SIGN);
        
        if (block.getState() instanceof Sign sign) {
            String prefix = plugin.getConfig().getString("messages.prefix", "&6[SkittleCities]&r ");
            
            if (region.getType() == Region.RegionType.FOR_HIRE) {
                sign.setLine(0, colorize(plugin.getConfig().getString("messages.sign-for-hire")));
                sign.setLine(1, colorize(plugin.getConfig().getString("messages.sign-price")
                        .replace("%price%", String.valueOf(region.getPrice()))));
                sign.setLine(2, colorize(plugin.getConfig().getString("messages.sign-duration")
                        .replace("%days%", String.valueOf(region.getLeaseDays()))));
                sign.setLine(3, colorize(plugin.getConfig().getString("messages.sign-click")));
            } else if (region.getType() == Region.RegionType.FOR_SALE) {
                sign.setLine(0, colorize(plugin.getConfig().getString("messages.sign-for-sale")));
                sign.setLine(1, colorize(plugin.getConfig().getString("messages.sign-price")
                        .replace("%price%", String.valueOf(region.getPrice()))));
                sign.setLine(2, "");
                sign.setLine(3, colorize(plugin.getConfig().getString("messages.sign-click")));
            }
            
            sign.update();
        }
    }

    private String colorize(String message) {
        return message.replace("&", "§");
    }
}
