package twotoz.skittleCities.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockIgniteEvent;
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

import java.util.UUID;

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
            // IN A CLAIM - get the effective owner (parent owner for subclaims)
            UUID effectiveOwner = getEffectiveOwner(region);
            
            if (effectiveOwner == null) {
                // NO OWNER (unclaimed) - check flag
                if (!plugin.getFlagManager().getClaimFlag(region, "block-break")) {
                    event.setCancelled(true);
                    MessageUtil.sendProtected(player, plugin.getConfig(), "skittlecities.admin", "/cignoreclaims");
                }
            } else {
                // HAS OWNER - check if player has access
                if (hasAccessToParent(region, player.getUniqueId())) {
                    // Owner/Trusted = ALWAYS ALLOWED
                    return;
                }
                
                // NOT owner/trusted - check flag
                if (!plugin.getFlagManager().getClaimFlag(region, "block-break")) {
                    event.setCancelled(true);
                    MessageUtil.sendProtected(player, plugin.getConfig(), "skittlecities.admin", "/cignoreclaims");
                }
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
            // IN A CLAIM - get the effective owner
            UUID effectiveOwner = getEffectiveOwner(region);
            
            if (effectiveOwner == null) {
                // NO OWNER - check flag
                if (!plugin.getFlagManager().getClaimFlag(region, "block-place")) {
                    event.setCancelled(true);
                    MessageUtil.sendProtected(player, plugin.getConfig(), "skittlecities.admin", "/cignoreclaims");
                }
            } else {
                // HAS OWNER - check if player has access
                if (hasAccessToParent(region, player.getUniqueId())) {
                    // Owner/Trusted = ALWAYS ALLOWED
                    return;
                }
                
                // NOT owner/trusted - check flag
                if (!plugin.getFlagManager().getClaimFlag(region, "block-place")) {
                    event.setCancelled(true);
                    MessageUtil.sendProtected(player, plugin.getConfig(), "skittlecities.admin", "/cignoreclaims");
                }
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
            // IN A CLAIM
            UUID effectiveOwner = getEffectiveOwner(region);
            
            if (effectiveOwner == null) {
                // NO OWNER - block sign editing
                event.setCancelled(true);
                MessageUtil.sendProtected(player, plugin.getConfig(), "skittlecities.admin", "/cignoreclaims");
            } else {
                // HAS OWNER - only owner/trusted can edit signs
                if (!hasAccessToParent(region, player.getUniqueId())) {
                    event.setCancelled(true);
                    MessageUtil.sendProtected(player, plugin.getConfig(), "skittlecities.admin", "/cignoreclaims");
                }
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
                // IN CLAIM
                UUID effectiveOwner = getEffectiveOwner(region);
                
                if (effectiveOwner != null && hasAccessToParent(region, player.getUniqueId())) {
                    // Owner/Trusted = ALWAYS ALLOWED
                    return;
                }
                
                // Check flag
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

        // CHECK FOR SPECIAL INTERACTIONS
        if (plugin.getIgnoreClaimsCommand().isBypassing(player.getUniqueId())) return;

        Material material = event.getClickedBlock().getType();
        
        // DOORS - Special handling (DEFAULT ALLOWED everywhere)
        if (isDoor(material)) {
            handleDoorInteraction(event, player);
            return;
        }
        
        // TRAPDOORS - Special handling (DEFAULT BLOCKED outside claims)
        if (isTrapdoor(material)) {
            handleTrapdoorInteraction(event, player);
            return;
        }
        
        // OTHER INTERACTIONS (chests, buttons, levers)
        String interactionType = getInteractionType(material);
        if (interactionType == null) return;

        Region region = plugin.getRegionManager().getRegionAt(event.getClickedBlock().getLocation());

        if (region != null) {
            // IN A CLAIM
            UUID effectiveOwner = getEffectiveOwner(region);
            
            if (effectiveOwner == null) {
                // NO OWNER - check flag
                if (!plugin.getFlagManager().getClaimFlag(region, interactionType)) {
                    event.setCancelled(true);
                    MessageUtil.sendProtected(player, plugin.getConfig(), "skittlecities.admin", "/cignoreclaims");
                }
            } else {
                // HAS OWNER - check if player has access
                if (hasAccessToParent(region, player.getUniqueId())) {
                    // Owner/Trusted = ALWAYS ALLOWED
                    return;
                }
                
                // NOT owner/trusted - check flag
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
    
    /**
     * Handle door interactions - DEFAULT ALLOWED (only block if flag explicitly disables)
     */
    private void handleDoorInteraction(PlayerInteractEvent event, Player player) {
        Region region = plugin.getRegionManager().getRegionAt(event.getClickedBlock().getLocation());
        
        if (region != null) {
            // IN A CLAIM
            UUID effectiveOwner = getEffectiveOwner(region);
            
            // Owner/Trusted always allowed
            if (effectiveOwner != null && hasAccessToParent(region, player.getUniqueId())) {
                return; // ALLOW
            }
            
            // Check use-doors flag - if FALSE, block
            if (!plugin.getFlagManager().getClaimFlag(region, "use-doors")) {
                event.setCancelled(true);
                MessageUtil.sendProtected(player, plugin.getConfig(), "skittlecities.admin", "/cignoreclaims");
            }
            // If flag is TRUE or not set → ALLOW (default behavior)
        }
        // OUTSIDE claims → ALWAYS ALLOW (no world flag check for doors)
    }
    
    /**
     * Handle trapdoor interactions - DEFAULT BLOCKED outside claims
     */
    private void handleTrapdoorInteraction(PlayerInteractEvent event, Player player) {
        Region region = plugin.getRegionManager().getRegionAt(event.getClickedBlock().getLocation());
        
        if (region != null) {
            // IN A CLAIM
            UUID effectiveOwner = getEffectiveOwner(region);
            
            // Owner/Trusted always allowed
            if (effectiveOwner != null && hasAccessToParent(region, player.getUniqueId())) {
                return; // ALLOW
            }
            
            // Check use-trapdoors flag
            if (!plugin.getFlagManager().getClaimFlag(region, "use-trapdoors")) {
                event.setCancelled(true);
                MessageUtil.sendProtected(player, plugin.getConfig(), "skittlecities.admin", "/cignoreclaims");
            }
        } else {
            // OUTSIDE claims → DEFAULT BLOCKED
            event.setCancelled(true);
            MessageUtil.sendProtected(player, plugin.getConfig(), "skittlecities.admin", "/cignoreclaims");
        }
    }
    
    /**
     * Check if material is a door
     */
    private boolean isDoor(Material material) {
        return switch (material) {
            case OAK_DOOR, SPRUCE_DOOR, BIRCH_DOOR, JUNGLE_DOOR, ACACIA_DOOR,
                 DARK_OAK_DOOR, CRIMSON_DOOR, WARPED_DOOR, IRON_DOOR,
                 OAK_FENCE_GATE, SPRUCE_FENCE_GATE, BIRCH_FENCE_GATE,
                 JUNGLE_FENCE_GATE, ACACIA_FENCE_GATE, DARK_OAK_FENCE_GATE,
                 CRIMSON_FENCE_GATE, WARPED_FENCE_GATE -> true;
            default -> false;
        };
    }
    
    /**
     * Check if material is a trapdoor
     */
    private boolean isTrapdoor(Material material) {
        return switch (material) {
            case OAK_TRAPDOOR, SPRUCE_TRAPDOOR, BIRCH_TRAPDOOR, JUNGLE_TRAPDOOR,
                 ACACIA_TRAPDOOR, DARK_OAK_TRAPDOOR, CRIMSON_TRAPDOOR,
                 WARPED_TRAPDOOR, IRON_TRAPDOOR -> true;
            default -> false;
        };
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof Player victim)) return;
        
        if (!isInConfiguredWorld(attacker)) return;
        if (plugin.getIgnoreClaimsCommand().isBypassing(attacker.getUniqueId())) return;

        Region region = plugin.getRegionManager().getRegionAt(victim.getLocation());

        if (region != null) {
            // IN A CLAIM - check PVP flag
            if (!plugin.getFlagManager().getClaimFlag(region, "pvp")) {
                event.setCancelled(true);
            }
        } else {
            // OUTSIDE claims - check world PVP
            if (!plugin.getFlagManager().getWorldFlag("pvp")) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        // Only check for mobs
        if (event.getEntity() instanceof Player) return;
        
        String worldName = plugin.getConfig().getString("world-name");
        if (!event.getLocation().getWorld().getName().equals(worldName)) return;

        Region region = plugin.getRegionManager().getRegionAt(event.getLocation());

        if (region != null) {
            // IN A CLAIM - check mob-spawning flag
            if (!plugin.getFlagManager().getClaimFlag(region, "mob-spawning")) {
                event.setCancelled(true);
            }
        } else {
            // OUTSIDE claims - check world flag
            if (!plugin.getFlagManager().getWorldFlag("mob-spawning")) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockSpread(BlockSpreadEvent event) {
        String worldName = plugin.getConfig().getString("world-name");
        if (!event.getBlock().getWorld().getName().equals(worldName)) return;

        Region region = plugin.getRegionManager().getRegionAt(event.getBlock().getLocation());

        if (region != null) {
            // IN A CLAIM
            if (!plugin.getFlagManager().getClaimFlag(region, "block-spread")) {
                event.setCancelled(true);
            }
        } else {
            // OUTSIDE claims
            if (!plugin.getFlagManager().getWorldFlag("block-spread")) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockGrow(BlockGrowEvent event) {
        String worldName = plugin.getConfig().getString("world-name");
        if (!event.getBlock().getWorld().getName().equals(worldName)) return;

        Region region = plugin.getRegionManager().getRegionAt(event.getBlock().getLocation());

        if (region != null) {
            if (!plugin.getFlagManager().getClaimFlag(region, "crop-growth")) {
                event.setCancelled(true);
            }
        } else {
            if (!plugin.getFlagManager().getWorldFlag("crop-growth")) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onStructureGrow(StructureGrowEvent event) {
        String worldName = plugin.getConfig().getString("world-name");
        if (!event.getLocation().getWorld().getName().equals(worldName)) return;

        Region region = plugin.getRegionManager().getRegionAt(event.getLocation());

        if (region != null) {
            if (!plugin.getFlagManager().getClaimFlag(region, "crop-growth")) {
                event.setCancelled(true);
            }
        } else {
            if (!plugin.getFlagManager().getWorldFlag("crop-growth")) {
                event.setCancelled(true);
            }
        }
    }
    
    /**
     * Get effective owner - for subclaims, returns parent owner
     */
    private UUID getEffectiveOwner(Region region) {
        if (region.isSubclaim()) {
            Region parent = plugin.getRegionManager().getParentClaim(region);
            return parent != null ? parent.getOwner() : null;
        }
        return region.getOwner();
    }
    
    /**
     * Check if player has access to claim (checks parent for subclaims)
     */
    private boolean hasAccessToParent(Region region, UUID playerId) {
        if (region.isSubclaim()) {
            Region parent = plugin.getRegionManager().getParentClaim(region);
            if (parent != null) {
                return plugin.getTrustManager().hasAccess(parent, playerId);
            }
            return false;
        }
        return plugin.getTrustManager().hasAccess(region, playerId);
    }

    private boolean isInConfiguredWorld(Player player) {
        String worldName = plugin.getConfig().getString("world-name");
        return player.getWorld().getName().equals(worldName);
    }

    private boolean isClaimSign(org.bukkit.block.Block block) {
        if (!(block.getState() instanceof org.bukkit.block.Sign sign)) return false;
        
        String line0 = sign.getLine(0);
        if (line0 == null) return false;
        
        String stripped = org.bukkit.ChatColor.stripColor(line0).toLowerCase();
        return stripped.contains("for sale") || stripped.contains("for hire");
    }

    private String getInteractionType(Material material) {
        return switch (material) {
            case CHEST, TRAPPED_CHEST, BARREL, SHULKER_BOX, ENDER_CHEST,
                 BLACK_SHULKER_BOX, BLUE_SHULKER_BOX, BROWN_SHULKER_BOX,
                 CYAN_SHULKER_BOX, GRAY_SHULKER_BOX, GREEN_SHULKER_BOX,
                 LIGHT_BLUE_SHULKER_BOX, LIGHT_GRAY_SHULKER_BOX, LIME_SHULKER_BOX,
                 MAGENTA_SHULKER_BOX, ORANGE_SHULKER_BOX, PINK_SHULKER_BOX,
                 PURPLE_SHULKER_BOX, RED_SHULKER_BOX, WHITE_SHULKER_BOX,
                 YELLOW_SHULKER_BOX -> "chest-access";
            case STONE_BUTTON, OAK_BUTTON, SPRUCE_BUTTON, BIRCH_BUTTON,
                 JUNGLE_BUTTON, ACACIA_BUTTON, DARK_OAK_BUTTON, CRIMSON_BUTTON,
                 WARPED_BUTTON, POLISHED_BLACKSTONE_BUTTON -> "use-buttons";
            case LEVER -> "use-levers";
            default -> null;
        };
    }
    
    @EventHandler
    public void onBlockBurn(BlockBurnEvent event) {
        if (!isInConfiguredWorld(event.getBlock().getWorld())) return;
        
        Region region = plugin.getRegionManager().getRegionAt(event.getBlock().getLocation());
        
        if (region != null) {
            // IN CLAIM - check flag
            if (!plugin.getFlagManager().getClaimFlag(region, "fire-spread")) {
                event.setCancelled(true);
            }
        } else {
            // OUTSIDE claims - check world flag
            if (!plugin.getFlagManager().getWorldFlag("fire-spread")) {
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent event) {
        if (!isInConfiguredWorld(event.getBlock().getWorld())) return;
        
        // Allow flint & steel and fire charges (player controlled)
        if (event.getCause() == BlockIgniteEvent.IgniteCause.FLINT_AND_STEEL ||
            event.getCause() == BlockIgniteEvent.IgniteCause.FIREBALL) {
            return;
        }
        
        // Block natural fire spread (lava, fire, lightning)
        Region region = plugin.getRegionManager().getRegionAt(event.getBlock().getLocation());
        
        if (region != null) {
            // IN CLAIM - check flag
            if (!plugin.getFlagManager().getClaimFlag(region, "fire-spread")) {
                event.setCancelled(true);
            }
        } else {
            // OUTSIDE claims - check world flag
            if (!plugin.getFlagManager().getWorldFlag("fire-spread")) {
                event.setCancelled(true);
            }
        }
    }
    
    private boolean isInConfiguredWorld(org.bukkit.World world) {
        String configWorld = plugin.getConfig().getString("world-name", "city");
        return world.getName().equalsIgnoreCase(configWorld);
    }
}
