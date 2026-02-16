package twotoz.skittleCities.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import twotoz.skittleCities.SkittleCities;
import twotoz.skittleCities.data.Region;
import twotoz.skittleCities.utils.MessageUtil;

import java.util.List;

public class SubclaimsCommand implements CommandExecutor {
    private final SkittleCities plugin;

    public SubclaimsCommand(SkittleCities plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtil.send(sender, plugin.getConfig(), "player-only");
            return true;
        }

        // ADMIN ONLY
        if (!player.hasPermission("skittlecities.admin")) {
            MessageUtil.send(player, plugin.getConfig(), "no-permission");
            return true;
        }

        // Get region at player location
        Region region = plugin.getRegionManager().getRegionAt(player.getLocation());
        
        if (region == null) {
            MessageUtil.send(player, plugin.getConfig(), "not-in-claim");
            return true;
        }

        // If player is in a subclaim, get parent
        if (region.isSubclaim()) {
            Region parent = plugin.getRegionManager().getParentClaim(region);
            if (parent != null) {
                region = parent;
                player.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
                    "&7You're in a subclaim. Showing subclaims of parent claim..."));
            }
        }

        // Get subclaims (admin can view any claim's subclaims)
        List<Region> subclaims = plugin.getRegionManager().getSubclaims(region.getId());

        String claimName = region.getDisplayName() != null ? region.getDisplayName() : region.getName();
        
        if (subclaims.isEmpty()) {
            player.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
                "&eClaim &a" + claimName + " &ehas no subclaims."));
            player.sendMessage(MessageUtil.colorize("&7Create one with: &e/csubclaim <name>"));
            return true;
        }

        player.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
            "&aSubclaims of &e" + claimName + "&a:"));
        player.sendMessage(MessageUtil.colorize("&7─────────────────────────────"));

        for (Region subclaim : subclaims) {
            String subName = subclaim.getDisplayName() != null ? subclaim.getDisplayName() : subclaim.getName();
            
            // Calculate size
            int width = Math.abs(subclaim.getMaxX() - subclaim.getMinX()) + 1;
            int height = Math.abs(subclaim.getMaxY() - subclaim.getMinY()) + 1;
            int depth = Math.abs(subclaim.getMaxZ() - subclaim.getMinZ()) + 1;
            
            player.sendMessage(MessageUtil.colorize("&e● " + subName));
            player.sendMessage(MessageUtil.colorize("  &7Size: &f" + width + "x" + height + "x" + depth));
            
            // Show some key flags that differ from parent
            boolean parentPvp = plugin.getFlagManager().getClaimFlag(region, "pvp");
            boolean subPvp = plugin.getFlagManager().getClaimFlag(subclaim, "pvp");
            
            boolean parentBreak = plugin.getFlagManager().getClaimFlag(region, "block-break");
            boolean subBreak = plugin.getFlagManager().getClaimFlag(subclaim, "block-break");
            
            if (parentPvp != subPvp || parentBreak != subBreak) {
                player.sendMessage(MessageUtil.colorize("  &7Overrides: &f" + 
                    (parentPvp != subPvp ? "pvp:" + (subPvp ? "ON" : "OFF") + " " : "") +
                    (parentBreak != subBreak ? "break:" + (subBreak ? "ON" : "OFF") : "")));
            }
        }

        player.sendMessage(MessageUtil.colorize("&7─────────────────────────────"));
        player.sendMessage(MessageUtil.colorize("&7Total: &e" + subclaims.size() + " &7subclaim" + 
            (subclaims.size() != 1 ? "s" : "")));

        return true;
    }
}
