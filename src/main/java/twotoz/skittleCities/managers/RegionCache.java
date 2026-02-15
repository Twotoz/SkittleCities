package twotoz.skittleCities.managers;

import org.bukkit.Location;
import twotoz.skittleCities.data.Region;

import java.util.*;

public class RegionCache {
    private final Map<String, Region> locationCache;
    private final List<Region> regions;
    private static final long CACHE_DURATION = 5000; // 5 seconds
    private final Map<String, Long> cacheTimestamps;

    public RegionCache() {
        this.locationCache = new HashMap<>();
        this.regions = new ArrayList<>();
        this.cacheTimestamps = new HashMap<>();
    }

    public void setRegions(List<Region> regions) {
        this.regions.clear();
        this.regions.addAll(regions);
        invalidateAll();
    }

    public Region getRegionAt(Location location) {
        String key = getLocationKey(location);
        long now = System.currentTimeMillis();
        
        // Check if cache is valid
        Long timestamp = cacheTimestamps.get(key);
        if (timestamp != null && now - timestamp < CACHE_DURATION) {
            return locationCache.get(key);
        }

        // Search for region
        Region found = null;
        for (Region region : regions) {
            if (region.contains(location)) {
                found = region;
                break;
            }
        }

        // Cache result
        locationCache.put(key, found);
        cacheTimestamps.put(key, now);
        
        return found;
    }

    public List<Region> getAllRegions() {
        return new ArrayList<>(regions);
    }

    public void invalidate(Location location) {
        String key = getLocationKey(location);
        locationCache.remove(key);
        cacheTimestamps.remove(key);
    }

    public void invalidateAll() {
        locationCache.clear();
        cacheTimestamps.clear();
    }

    private String getLocationKey(Location loc) {
        return loc.getWorld().getName() + ":" + 
               loc.getBlockX() + ":" + 
               loc.getBlockY() + ":" + 
               loc.getBlockZ();
    }
}
