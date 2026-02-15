package twotoz.skittleCities.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import twotoz.skittleCities.SkittleCities;
import twotoz.skittleCities.data.Region;
import twotoz.skittleCities.utils.MessageUtil;

public class ProtectionListener implements Listener {
    private final SkittleCities plugin;

    public ProtectionListener(SkittleCities plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        
        // Check if in configured world
        if (!isInConfiguredWorld(player)) return;

        // Protect claim signs
        if (isClaimSign(event.getBlock())) {
            if (!plugin.getIgnoreClaimsCommand().isBypassing(player.getUniqueId())) {
                event.setCancelled(true);
                MessageUtil.sendProtected(player, plugin.getConfig(), "skittlecities.admin", "/cignoreclaims");
                return;
            }
        }

        // Check if bypassing
        if (plugin.getIgnoreClaimsCommand().isBypassing(player.getUniqueId())) return;

        Region region = plugin.getRegionManager().getRegionAt(event.getBlock().getLocation());

        if (region != null) {
            // In a claim
            if (!plugin.getTrustManager().hasAccess(region, player.getUniqueId())) {
                if (!plugin.getFlagManager().getClaimFlag(region, "block-break")) {
                    event.setCancelled(true);
                    MessageUtil.sendProtected(player, plugin.getConfig(), "skittlecities.admin", "/cignoreclaims");
                }
            }
        } else {
            // Outside claims (world protection)
            if (!plugin.getFlagManager().getWorldFlag("block-break")) {
                event.setCancelled(true);
                MessageUtil.sendProtected(player, plugin.getConfig(), "skittlecities.admin", "/cignoreclaims");
            }
        }
    }

    private boolean isClaimSign(org.bukkit.block.Block block) {
        if (!(block.getState() instanceof org.bukkit.block.Sign)) return false;
        
        // Check if this sign is a claim sign
        for (Region r : plugin.getRegionManager().getAllRegions()) {
            if (r.getSignLocation() != null && 
                r.getSignLocation().getBlockX() == block.getX() &&
                r.getSignLocation().getBlockY() == block.getY() &&
                r.getSignLocation().getBlockZ() == block.getZ()) {
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        
        if (!isInConfiguredWorld(player)) return;
        if (plugin.getIgnoreClaimsCommand().isBypassing(player.getUniqueId())) return;

        Region region = plugin.getRegionManager().getRegionAt(event.getBlock().getLocation());

        if (region != null) {
            if (!plugin.getTrustManager().hasAccess(region, player.getUniqueId())) {
                if (!plugin.getFlagManager().getClaimFlag(region, "block-place")) {
                    event.setCancelled(true);
                    MessageUtil.sendProtected(player, plugin.getConfig(), "skittlecities.admin", "/cignoreclaims");
                }
            }
        } else {
            if (!plugin.getFlagManager().getWorldFlag("block-place")) {
                event.setCancelled(true);
                MessageUtil.sendProtected(player, plugin.getConfig(), "skittlecities.admin", "/cignoreclaims");
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof Player victim)) return;

        if (!isInConfiguredWorld(attacker)) return;

        Region region = plugin.getRegionManager().getRegionAt(victim.getLocation());

        if (region != null) {
            if (!plugin.getFlagManager().getClaimFlag(region, "pvp")) {
                event.setCancelled(true);
            }
        } else {
            if (!plugin.getFlagManager().getWorldFlag("pvp")) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (!event.getLocation().getWorld().getName().equals(
                plugin.getConfig().getString("world-name"))) return;

        Region region = plugin.getRegionManager().getRegionAt(event.getLocation());

        if (region != null) {
            if (!plugin.getFlagManager().getClaimFlag(region, "mob-spawning")) {
                event.setCancelled(true);
            }
        } else {
            if (!plugin.getFlagManager().getWorldFlag("mob-spawning")) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getClickedBlock() == null) return;
        
        if (!isInConfiguredWorld(player)) return;
        if (plugin.getIgnoreClaimsCommand().isBypassing(player.getUniqueId())) return;

        Region region = plugin.getRegionManager().getRegionAt(event.getClickedBlock().getLocation());

        String interactionType = getInteractionType(event.getClickedBlock().getType());
        if (interactionType == null) return;

        if (region != null) {
            if (!plugin.getTrustManager().hasAccess(region, player.getUniqueId())) {
                if (!plugin.getFlagManager().getClaimFlag(region, interactionType)) {
                    event.setCancelled(true);
                    MessageUtil.sendProtected(player, plugin.getConfig(), "skittlecities.admin", "/cignoreclaims");
                }
            }
        } else {
            if (!plugin.getFlagManager().getWorldFlag(interactionType)) {
                event.setCancelled(true);
                MessageUtil.sendProtected(player, plugin.getConfig(), "skittlecities.admin", "/cignoreclaims");
            }
        }
    }

    private String getInteractionType(org.bukkit.Material material) {
        String name = material.name();
        if (name.contains("DOOR")) return "use-doors";
        if (name.contains("BUTTON")) return "use-buttons";
        if (name.contains("LEVER")) return "use-levers";
        if (name.contains("CHEST") || name.contains("BARREL") || name.contains("SHULKER")) return "chest-access";
        return null;
    }

    private boolean isInConfiguredWorld(Player player) {
        return player.getWorld().getName().equals(plugin.getConfig().getString("world-name"));
    }
}
