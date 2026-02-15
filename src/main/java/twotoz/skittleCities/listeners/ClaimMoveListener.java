package twotoz.skittleCities.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import twotoz.skittleCities.SkittleCities;
import twotoz.skittleCities.data.Region;
import twotoz.skittleCities.utils.MessageUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClaimMoveListener implements Listener {
    private final SkittleCities plugin;
    private final Map<UUID, Region> playerLastRegion;

    public ClaimMoveListener(SkittleCities plugin) {
        this.plugin = plugin;
        this.playerLastRegion = new HashMap<>();
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
            if (currentRegion == null && lastRegion != null) {
                // Left a claim
                handleClaimLeave(player, lastRegion);
            } else if (currentRegion != null && lastRegion == null) {
                // Entered a claim
                handleClaimEnter(player, currentRegion);
            } else if (currentRegion != null && lastRegion != null) {
                // Moved from one claim to another
                handleClaimLeave(player, lastRegion);
                handleClaimEnter(player, currentRegion);
            }

            playerLastRegion.put(player.getUniqueId(), currentRegion);
        }
    }

    private void handleClaimEnter(Player player, Region region) {
        // Special message for safezone
        if (region.getType() == Region.RegionType.SAFEZONE) {
            String message = MessageUtil.colorize("&aEntered safezone");
            plugin.getActionBarManager().sendTemporary(player, message);
            return;
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

    public void cleanup(Player player) {
        playerLastRegion.remove(player.getUniqueId());
    }
}
