package twotoz.skittleCities.tasks;

import org.bukkit.OfflinePlayer;
import twotoz.skittleCities.SkittleCities;
import twotoz.skittleCities.data.Region;

import java.util.List;

public class LeaseCheckTask implements Runnable {
    private final SkittleCities plugin;

    public LeaseCheckTask(SkittleCities plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        List<Region> regions = plugin.getRegionManager().getAllRegions();

        for (Region region : regions) {
            if (region.getType() != Region.RegionType.FOR_HIRE) continue;
            if (region.getOwner() == null) continue;

            if (region.isExpired()) {
                handleExpiredLease(region);
            }
        }
    }

    private void handleExpiredLease(Region region) {
        // Check if auto-extend is enabled
        if (region.isAutoExtend()) {
            // Check if owner has enough balance
            if (plugin.getEconomyManager().hasBalance(region.getOwner(), region.getPrice())) {
                // Auto-renew
                plugin.getEconomyManager().withdraw(region.getOwner(), region.getPrice());
                plugin.getRegionManager().handleLeaseRenewal(region);

                // Notify owner if online
                OfflinePlayer owner = plugin.getServer().getOfflinePlayer(region.getOwner());
                if (owner.isOnline() && owner.getPlayer() != null) {
                    owner.getPlayer().sendMessage(
                        plugin.getConfig().getString("messages.prefix") +
                        "§aYour lease was automatically renewed for §e$" + region.getPrice()
                    );
                }
                return;
            }
        }

        // Lease expired, notify and reset
        OfflinePlayer owner = plugin.getServer().getOfflinePlayer(region.getOwner());
        if (owner.isOnline() && owner.getPlayer() != null) {
            owner.getPlayer().sendMessage(
                plugin.getConfig().getString("messages.prefix") +
                plugin.getConfig().getString("messages.claim-expired")
            );
        }

        plugin.getRegionManager().handleLeaseExpiry(region);
    }
}
