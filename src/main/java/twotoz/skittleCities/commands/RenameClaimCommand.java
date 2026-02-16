package twotoz.skittleCities.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import twotoz.skittleCities.SkittleCities;
import twotoz.skittleCities.data.Region;
import twotoz.skittleCities.utils.MessageUtil;

public class RenameClaimCommand implements CommandExecutor {
    private final SkittleCities plugin;

    public RenameClaimCommand(SkittleCities plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtil.send(sender, plugin.getConfig(), "player-only");
            return true;
        }

        if (!player.hasPermission("skittlecities.rename")) {
            MessageUtil.send(player, plugin.getConfig(), "no-permission");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
                "&cUsage: /crename <name>"));
            player.sendMessage(MessageUtil.colorize("&7Stand in your claim and give it a custom name!"));
            player.sendMessage(MessageUtil.colorize("&7Example: &e/crename My Epic Base"));
            return true;
        }

        // Get region at player location
        Region region = plugin.getRegionManager().getRegionAt(player.getLocation());
        
        if (region == null) {
            MessageUtil.send(player, plugin.getConfig(), "not-in-claim");
            return true;
        }

        // Check ownership
        if (region.getOwner() == null || !region.getOwner().equals(player.getUniqueId())) {
            MessageUtil.send(player, plugin.getConfig(), "not-claim-owner");
            return true;
        }

        // Build new display name from args
        String displayName = String.join(" ", args);
        
        // Limit length
        if (displayName.length() > 32) {
            player.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
                "&cName too long! Max 32 characters."));
            return true;
        }

        // Set display name
        region.setDisplayName(displayName);
        plugin.getRegionManager().updateRegion(region);

        player.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
            "&aClaim renamed to: &e" + displayName));
        
        if (region.isSubclaim()) {
            Region parent = plugin.getRegionManager().getParentClaim(region);
            if (parent != null) {
                player.sendMessage(MessageUtil.colorize("&7This is a subclaim of: &e" + 
                    (parent.getDisplayName() != null ? parent.getDisplayName() : parent.getName())));
            }
        }

        return true;
    }
}
