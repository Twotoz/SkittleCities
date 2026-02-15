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
            if (existing.overlaps(newRegion)) {
                return true;
            }
        }
        return false;
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
        regions.remove(region);
        plugin.getDatabaseManager().deleteRegion(region.getId());
        regionCache.setRegions(regions);
        
        // Remove sign if exists
        if (region.getSignLocation() != null) {
            region.getSignLocation().getBlock().setType(org.bukkit.Material.AIR);
        }
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
