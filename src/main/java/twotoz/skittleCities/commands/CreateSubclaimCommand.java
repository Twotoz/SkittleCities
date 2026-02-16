package twotoz.skittleCities.commands;

import org.bukkit.Location;
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

        if (!player.hasPermission("skittlecities.subclaim")) {
            MessageUtil.send(player, plugin.getConfig(), "no-permission");
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
                "&cUsage: /csubclaim <name>"));
            player.sendMessage(MessageUtil.colorize("&7Create a subclaim within your claim!"));
            player.sendMessage(MessageUtil.colorize("&71. Use &e/ctool &7to select area"));
            player.sendMessage(MessageUtil.colorize("&72. Stand in parent claim"));
            player.sendMessage(MessageUtil.colorize("&73. Run &e/csubclaim <name>"));
            player.sendMessage(MessageUtil.colorize("&7"));
            player.sendMessage(MessageUtil.colorize("&eSubclaims override parent flags!"));
            return true;
        }

        String name = String.join("_", args);

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
            return true;
        }

        // Check ownership of parent
        if (parentRegion.getOwner() == null || !parentRegion.getOwner().equals(player.getUniqueId())) {
            player.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
                "&cYou don't own this claim!"));
            return true;
        }

        // Create subclaim region
        Region subclaim = new Region(0, name, player.getWorld(), selection.getPos1(), selection.getPos2());
        subclaim.setOwner(parentRegion.getOwner()); // SAME OWNER AS PARENT!
        subclaim.setType(Region.RegionType.PRIVATE);
        subclaim.setParentId(parentRegion.getId()); // SET PARENT!

        // Verify subclaim is completely inside parent
        if (!isCompletelyInside(subclaim, parentRegion)) {
            player.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
                "&cSubclaim must be completely inside the parent claim!"));
            return true;
        }

        // Check overlap with OTHER subclaims (not parent)
        for (Region existing : plugin.getRegionManager().getAllRegions()) {
            // Skip the parent (subclaim is inside parent by design)
            if (existing.getId() == parentRegion.getId()) continue;
            
            // Skip if existing is also a subclaim of same parent (we'll check volume priority)
            if (existing.getParentId() != null && existing.getParentId() == parentRegion.getId()) {
                // Allow if new subclaim is smaller (will have priority)
                continue;
            }
            
            // Check partial overlap
            if (subclaim.overlaps(existing) && !isCompletelyInside(subclaim, existing)) {
                player.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
                    "&cThis area overlaps with another claim!"));
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

        player.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
            "&aSubclaim &e" + name + " &acreated inside &e" + 
            (parentRegion.getDisplayName() != null ? parentRegion.getDisplayName() : parentRegion.getName())));
        player.sendMessage(MessageUtil.colorize("&7→ Owner: &eSame as parent claim"));
        player.sendMessage(MessageUtil.colorize("&7→ Flags: &eCopied from parent (customize with /cflags)"));
        player.sendMessage(MessageUtil.colorize("&7→ Trust: &eInherited from parent"));
        player.sendMessage(MessageUtil.colorize(""));
        player.sendMessage(MessageUtil.colorize("&eSubclaim flags OVERRIDE parent flags!"));
        player.sendMessage(MessageUtil.colorize("&7Example: Parent has block-break:false, subclaim can set block-break:true"));

        return true;
    }

    /**
     * Check if subclaim is completely inside parent
     */
    private boolean isCompletelyInside(Region inner, Region outer) {
        return inner.getMinX() >= outer.getMinX() &&
               inner.getMaxX() <= outer.getMaxX() &&
               inner.getMinY() >= outer.getMinY() &&
               inner.getMaxY() <= outer.getMaxY() &&
               inner.getMinZ() >= outer.getMinZ() &&
               inner.getMaxZ() <= outer.getMaxZ();
    }
}
