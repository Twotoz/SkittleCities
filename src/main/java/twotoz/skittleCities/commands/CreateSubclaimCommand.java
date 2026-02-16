package twotoz.skittleCities.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import twotoz.skittleCities.SkittleCities;
import twotoz.skittleCities.data.Region;
import twotoz.skittleCities.data.Selection;
import twotoz.skittleCities.utils.MessageUtil;

public class CreateSubclaimCommand implements CommandExecutor {
    private final SkittleCities plugin;

    public CreateSubclaimCommand(SkittleCities plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtil.send(sender, plugin.getConfig(), "player-only");
            return true;
        }

        // ADMIN ONLY - subclaims are admin tools!
        if (!player.hasPermission("skittlecities.admin")) {
            MessageUtil.send(player, plugin.getConfig(), "no-permission");
            return true;
        }

        // Get selection
        Selection selection = plugin.getSelectionManager().getSelection(player);
        if (selection == null || !selection.isComplete()) {
            player.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
                "&cPlease make a selection first using the &e/ctool&c!"));
            return true;
        }

        // Get parent region at player's location
        Region parentRegion = plugin.getRegionManager().getRegionAt(player.getLocation());
        
        if (parentRegion == null) {
            player.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
                "&cYou must stand inside the parent claim!"));
            player.sendMessage(MessageUtil.colorize("&7Subclaims must be created inside an existing claim."));
            return true;
        }

        // Don't allow subclaims of subclaims
        if (parentRegion.isSubclaim()) {
            player.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
                "&cYou cannot create a subclaim inside another subclaim!"));
            player.sendMessage(MessageUtil.colorize("&7Only create subclaims in main claims."));
            return true;
        }

        // Generate unique technical name
        String technicalName = generateSubclaimName(parentRegion);

        // Create subclaim region
        Region subclaim = new Region(0, technicalName, player.getWorld(), selection.getPos1(), selection.getPos2());
        subclaim.setOwner(parentRegion.getOwner()); // SAME OWNER AS PARENT!
        subclaim.setType(Region.RegionType.PRIVATE);
        subclaim.setParentId(parentRegion.getId()); // SET PARENT!
        // NO display name - uses parent name

        // Verify subclaim is completely inside parent
        if (!isCompletelyInside(subclaim, parentRegion)) {
            String parentName = parentRegion.getDisplayName() != null ? 
                parentRegion.getDisplayName() : parentRegion.getName();
            
            player.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
                "&cSubclaim must be completely inside the parent claim!"));
            player.sendMessage(MessageUtil.colorize("&7"));
            player.sendMessage(MessageUtil.colorize("&eYour selection extends outside of: &a" + parentName));
            player.sendMessage(MessageUtil.colorize("&7Selection is partly in claim and partly in wilderness."));
            player.sendMessage(MessageUtil.colorize("&7"));
            player.sendMessage(MessageUtil.colorize("&cYou cannot create a subclaim that extends outside the parent claim!"));
            player.sendMessage(MessageUtil.colorize("&7Make sure your selection is fully within the claim boundaries."));
            return true;
        }

        // Check overlap with OTHER claims (not parent)
        for (Region existing : plugin.getRegionManager().getAllRegions()) {
            // Skip parent claim
            if (existing.getId() == parentRegion.getId()) continue;
            
            // Check if subclaim overlaps with another claim
            if (subclaim.overlaps(existing)) {
                String existingName = existing.getDisplayName() != null ? 
                    existing.getDisplayName() : existing.getName();
                
                player.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
                    "&cYour subclaim overlaps with another claim: &e" + existingName));
                player.sendMessage(MessageUtil.colorize("&7"));
                player.sendMessage(MessageUtil.colorize("&cSubclaims cannot overlap with other claims!"));
                player.sendMessage(MessageUtil.colorize("&7Keep your subclaim within the parent claim boundaries."));
                return true;
            }
        }

        // Copy flags from parent as defaults (can be customized later)
        subclaim.setFlags(new java.util.HashMap<>(parentRegion.getFlags()));
        
        // Inherit trust from parent
        subclaim.setTrustedPlayers(new java.util.HashSet<>(parentRegion.getTrustedPlayers()));

        // Create subclaim
        plugin.getRegionManager().createRegion(subclaim);

        // Clear selection
        plugin.getSelectionManager().clearSelection(player);

        String parentName = parentRegion.getDisplayName() != null ? 
            parentRegion.getDisplayName() : parentRegion.getName();
        
        player.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
            "&aSubclaim created inside &e" + parentName));
        player.sendMessage(MessageUtil.colorize("&7→ Technical name: &f" + technicalName));
        player.sendMessage(MessageUtil.colorize("&7→ Uses parent name when displaying"));
        player.sendMessage(MessageUtil.colorize("&7→ Flags: &eCopied from parent (customize with /cflags)"));
        player.sendMessage(MessageUtil.colorize("&7"));
        player.sendMessage(MessageUtil.colorize("&eSubclaim flags OVERRIDE parent flags!"));

        return true;
    }
    
    /**
     * Generate unique technical name for subclaim
     */
    private String generateSubclaimName(Region parentRegion) {
        int counter = 1;
        String baseName = "subclaim_" + parentRegion.getId() + "_";
        
        while (true) {
            String name = baseName + counter;
            boolean exists = false;
            
            for (Region r : plugin.getRegionManager().getAllRegions()) {
                if (r.getName().equals(name)) {
                    exists = true;
                    break;
                }
            }
            
            if (!exists) {
                return name;
            }
            counter++;
        }
    }
    
    /**
     * Check if inner region is completely inside outer region
     */
    private boolean isCompletelyInside(Region inner, Region outer) {
        return inner.getMinX() >= outer.getMinX() && inner.getMaxX() <= outer.getMaxX() &&
               inner.getMinY() >= outer.getMinY() && inner.getMaxY() <= outer.getMaxY() &&
               inner.getMinZ() >= outer.getMinZ() && inner.getMaxZ() <= outer.getMaxZ();
    }
}
