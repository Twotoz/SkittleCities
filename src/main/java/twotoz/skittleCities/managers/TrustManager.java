package twotoz.skittleCities.managers;

import twotoz.skittleCities.SkittleCities;
import twotoz.skittleCities.data.Region;

import java.util.UUID;

public class TrustManager {
    private final SkittleCities plugin;

    public TrustManager(SkittleCities plugin) {
        this.plugin = plugin;
    }

    public boolean trustPlayer(Region region, UUID player) {
        if (region.getTrustedPlayers().contains(player)) {
            return false; // Already trusted
        }
        
        region.getTrustedPlayers().add(player);
        plugin.getRegionManager().updateRegion(region);
        return true;
    }

    public boolean untrustPlayer(Region region, UUID player) {
        if (!region.getTrustedPlayers().contains(player)) {
            return false; // Not trusted
        }
        
        region.getTrustedPlayers().remove(player);
        plugin.getRegionManager().updateRegion(region);
        return true;
    }

    public boolean isTrusted(Region region, UUID player) {
        return region.getTrustedPlayers().contains(player);
    }

    public boolean hasAccess(Region region, UUID player) {
        // Owner always has access
        if (region.getOwner() != null && region.getOwner().equals(player)) {
            return true;
        }
        
        // Trusted players have access
        return isTrusted(region, player);
    }
}
