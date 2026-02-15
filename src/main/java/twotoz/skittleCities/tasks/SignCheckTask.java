package twotoz.skittleCities.tasks;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;
import twotoz.skittleCities.SkittleCities;
import twotoz.skittleCities.data.Region;

import java.util.List;

public class SignCheckTask extends BukkitRunnable {
    private final SkittleCities plugin;

    public SignCheckTask(SkittleCities plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        List<Region> regions = plugin.getRegionManager().getAllRegions();

        for (Region region : regions) {
            // Skip if no sign location or if region type doesn't have signs
            if (region.getSignLocation() == null) continue;
            if (region.getType() == Region.RegionType.PRIVATE || 
                region.getType() == Region.RegionType.SAFEZONE) continue;
            
            // CRITICAL: Skip if region is owned - owned claims should NOT have signs!
            if (region.getOwner() != null) continue;

            Location signLoc = region.getSignLocation();
            Block block = signLoc.getBlock();

            // Check if sign is missing
            if (!isSign(block.getType())) {
                // Recreate the sign
                plugin.getRegionManager().recreateSign(region);
                plugin.getLogger().info("Auto-recovered missing sign for region: " + region.getName());
            }
        }
    }

    private boolean isSign(Material material) {
        return material == Material.OAK_WALL_SIGN || 
               material == Material.SPRUCE_WALL_SIGN ||
               material == Material.BIRCH_WALL_SIGN ||
               material == Material.JUNGLE_WALL_SIGN ||
               material == Material.ACACIA_WALL_SIGN ||
               material == Material.DARK_OAK_WALL_SIGN ||
               material == Material.CRIMSON_WALL_SIGN ||
               material == Material.WARPED_WALL_SIGN ||
               material == Material.OAK_SIGN ||
               material == Material.SPRUCE_SIGN ||
               material == Material.BIRCH_SIGN ||
               material == Material.JUNGLE_SIGN ||
               material == Material.ACACIA_SIGN ||
               material == Material.DARK_OAK_SIGN ||
               material == Material.CRIMSON_SIGN ||
               material == Material.WARPED_SIGN;
    }
}
