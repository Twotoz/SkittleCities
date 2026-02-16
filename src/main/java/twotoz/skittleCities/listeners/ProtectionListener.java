package twotoz.skittleCities.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.StructureGrowEvent;
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

        // Protect claim signs - ALWAYS, even with bypass
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
            // IN A CLAIM (claimed or unclaimed)
            // Rule: MUST have access (owner/trusted) to build
            if (!plugin.getTrustManager().hasAccess(region, player.getUniqueId())) {
                // No access = always blocked
                event.setCancelled(true);
                MessageUtil.sendProtected(player, plugin.getConfig(), "skittlecities.admin", "/cignoreclaims");
                return;
            }
            
            // Has access - check flag
            if (!plugin.getFlagManager().getClaimFlag(region, "block-break")) {
                event.setCancelled(true);
                MessageUtil.sendProtected(player, plugin.getConfig(), "skittlecities.admin", "/cignoreclaims");
            }
        } else {
            // OUTSIDE claims (wilderness)
            if (!plugin.getFlagManager().getWorldFlag("block-break")) {
                event.setCancelled(true);
                MessageUtil.sendProtected(player, plugin.getConfig(), "skittlecities.admin", "/cignoreclaims");
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        
        if (!isInConfiguredWorld(player)) return;
        if (plugin.getIgnoreClaimsCommand().isBypassing(player.getUniqueId())) return;

        Region region = plugin.getRegionManager().getRegionAt(event.getBlock().getLocation());

        if (region != null) {
            // IN A CLAIM - must have access
            if (!plugin.getTrustManager().hasAccess(region, player.getUniqueId())) {
                // No access = always blocked
                event.setCancelled(true);
                MessageUtil.sendProtected(player, plugin.getConfig(), "skittlecities.admin", "/cignoreclaims");
                return;
            }
            
            // Has access - check flag
            if (!plugin.getFlagManager().getClaimFlag(region, "block-place")) {
                event.setCancelled(true);
                MessageUtil.sendProtected(player, plugin.getConfig(), "skittlecities.admin", "/cignoreclaims");
            }
        } else {
            // OUTSIDE claims
            if (!plugin.getFlagManager().getWorldFlag("block-place")) {
                event.setCancelled(true);
                MessageUtil.sendProtected(player, plugin.getConfig(), "skittlecities.admin", "/cignoreclaims");
            }
        }
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();
        
        if (!isInConfiguredWorld(player)) return;
        if (plugin.getIgnoreClaimsCommand().isBypassing(player.getUniqueId())) return;

        Region region = plugin.getRegionManager().getRegionAt(event.getBlock().getLocation());

        if (region != null) {
            // IN A CLAIM - must have access to edit signs
            if (!plugin.getTrustManager().hasAccess(region, player.getUniqueId())) {
                event.setCancelled(true);
                MessageUtil.sendProtected(player, plugin.getConfig(), "skittlecities.admin", "/cignoreclaims");
            }
        } else {
            // OUTSIDE claims - check world flag
            if (!plugin.getFlagManager().getWorldFlag("block-place")) {
                event.setCancelled(true);
                MessageUtil.sendProtected(player, plugin.getConfig(), "skittlecities.admin", "/cignoreclaims");
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getClickedBlock() == null) return;
        
        if (!isInConfiguredWorld(player)) return;

        // FARMLAND TRAMPLING PROTECTION
        if (event.getAction() == org.bukkit.event.block.Action.PHYSICAL && 
            event.getClickedBlock().getType() == Material.FARMLAND) {
            
            if (plugin.getIgnoreClaimsCommand().isBypassing(player.getUniqueId())) return;
            
            Region region = plugin.getRegionManager().getRegionAt(event.getClickedBlock().getLocation());

            if (region != null) {
                // IN CLAIM - claim flag has priority
                if (!plugin.getFlagManager().getClaimFlag(region, "trampling")) {
                    event.setCancelled(true);
                    return;
                }
            } else {
                // OUTSIDE claims - use world flag
                if (!plugin.getFlagManager().getWorldFlag("trampling")) {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        // REGULAR INTERACTIONS (chests, doors, etc.)
        if (plugin.getIgnoreClaimsCommand().isBypassing(player.getUniqueId())) return;

        Material material = event.getClickedBlock().getType();
        String interactionType = getInteractionType(material);
        if (interactionType == null) return;

        Region region = plugin.getRegionManager().getRegionAt(event.getClickedBlock().getLocation());

        if (region != null) {
            // IN A CLAIM
            // Special rule for chests: MUST have access (owner/trusted)
            if (interactionType.equals("chest-access")) {
                if (!plugin.getTrustManager().hasAccess(region, player.getUniqueId())) {
                    // No access to claim = cannot open chests
                    event.setCancelled(true);
                    MessageUtil.sendProtected(player, plugin.getConfig(), "skittlecities.admin", "/cignoreclaims");
                    return;
                }
                // Has access - allow (chests always allowed for trusted players)
                return;
            }
            
            // Other interactions (doors/buttons/levers)
            if (!plugin.getTrustManager().hasAccess(region, player.getUniqueId())) {
                // No access = check flag
                if (!plugin.getFlagManager().getClaimFlag(region, interactionType)) {
                    event.setCancelled(true);
                    MessageUtil.sendProtected(player, plugin.getConfig(), "skittlecities.admin", "/cignoreclaims");
                }
            }
        } else {
            // OUTSIDE claims - check world flag
            if (!plugin.getFlagManager().getWorldFlag(interactionType)) {
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
        if (!isInConfiguredWorld(event.getLocation())) return;

        Region region = plugin.getRegionManager().getRegionAt(event.getLocation());

        if (region != null) {
            // IN CLAIM - claim flag has priority
            if (!plugin.getFlagManager().getClaimFlag(region, "mob-spawning")) {
                event.setCancelled(true);
            }
        } else {
            // OUTSIDE claims - use world flag
            if (!plugin.getFlagManager().getWorldFlag("mob-spawning")) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockGrow(org.bukkit.event.block.BlockGrowEvent event) {
        // Crops, saplings, vines, etc.
        if (!isInConfiguredWorld(event.getBlock().getLocation())) return;

        Region region = plugin.getRegionManager().getRegionAt(event.getBlock().getLocation());

        if (region != null) {
            // IN CLAIM - claim flag has priority
            if (!plugin.getFlagManager().getClaimFlag(region, "plant-growth")) {
                event.setCancelled(true);
            }
        } else {
            // OUTSIDE claims - use world flag
            if (!plugin.getFlagManager().getWorldFlag("plant-growth")) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockSpread(org.bukkit.event.block.BlockSpreadEvent event) {
        // Vines spreading, mushrooms, etc.
        if (!isInConfiguredWorld(event.getBlock().getLocation())) return;

        Region region = plugin.getRegionManager().getRegionAt(event.getBlock().getLocation());

        if (region != null) {
            // IN CLAIM - claim flag has priority
            if (!plugin.getFlagManager().getClaimFlag(region, "plant-growth")) {
                event.setCancelled(true);
            }
        } else {
            // OUTSIDE claims - use world flag
            if (!plugin.getFlagManager().getWorldFlag("plant-growth")) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onStructureGrow(org.bukkit.event.world.StructureGrowEvent event) {
        // Trees from saplings, huge mushrooms
        if (!isInConfiguredWorld(event.getLocation())) return;

        Region region = plugin.getRegionManager().getRegionAt(event.getLocation());

        if (region != null) {
            // IN CLAIM - claim flag has priority
            if (!plugin.getFlagManager().getClaimFlag(region, "plant-growth")) {
                event.setCancelled(true);
            }
        } else {
            // OUTSIDE claims - use world flag
            if (!plugin.getFlagManager().getWorldFlag("plant-growth")) {
                event.setCancelled(true);
            }
        }
    }

    private String getInteractionType(Material material) {
        String name = material.name();
        if (name.contains("DOOR")) return "use-doors";
        if (name.contains("BUTTON")) return "use-buttons";
        if (name.contains("LEVER")) return "use-levers";
        if (name.contains("CHEST") || name.contains("BARREL") || name.contains("SHULKER_BOX")) return "chest-access";
        if (name.contains("FURNACE") || name.contains("BLAST_FURNACE") || name.contains("SMOKER")) return "chest-access";
        if (name.contains("HOPPER") || name.contains("DROPPER") || name.contains("DISPENSER")) return "chest-access";
        return null;
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

    private boolean isInConfiguredWorld(Player player) {
        return player.getWorld().getName().equals(plugin.getConfig().getString("world-name"));
    }
    
    private boolean isInConfiguredWorld(org.bukkit.Location location) {
        return location.getWorld().getName().equals(plugin.getConfig().getString("world-name"));
    }
}
