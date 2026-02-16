package twotoz.skittleCities.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import twotoz.skittleCities.SkittleCities;
import twotoz.skittleCities.data.Selection;
import twotoz.skittleCities.gui.RegionCreateGUI;
import twotoz.skittleCities.utils.MessageUtil;

public class RegionCreateCommand implements CommandExecutor {
    private final SkittleCities plugin;

    public RegionCreateCommand(SkittleCities plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtil.send(sender, plugin.getConfig(), "player-only");
            return true;
        }

        if (!player.hasPermission("skittlecities.admin")) {
            MessageUtil.send(player, plugin.getConfig(), "no-permission");
            return true;
        }

        String worldName = plugin.getConfig().getString("world-name");
        if (!player.getWorld().getName().equals(worldName)) {
            MessageUtil.send(player, plugin.getConfig(), "wrong-world");
            return true;
        }

        Selection selection = plugin.getSelectionManager().getSelection(player);
        if (!selection.isComplete()) {
            MessageUtil.send(player, plugin.getConfig(), "no-selection");
            return true;
        }
        
        // Check if selection overlaps with existing claims
        var status = plugin.getRegionManager().checkSelection(selection.getPos1(), selection.getPos2());
        
        switch (status.getType()) {
            case INSIDE_CLAIM -> {
                // Selection is fully inside an existing claim
                var parentClaim = status.getRegion();
                String claimName = parentClaim.getDisplayName() != null ? 
                    parentClaim.getDisplayName() : parentClaim.getName();
                
                player.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
                    "&eRegion inside claim: &a" + claimName));
                player.sendMessage(MessageUtil.colorize("&7Use &e/csubclaim &7to make a subclaim."));
                return true;
            }
            case PARTIAL_OVERLAP -> {
                // Selection partially overlaps with a claim
                var overlappingClaim = status.getRegion();
                String claimName = overlappingClaim.getDisplayName() != null ? 
                    overlappingClaim.getDisplayName() : overlappingClaim.getName();
                
                player.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
                    "&cYour selection overlaps with claim: &e" + claimName));
                player.sendMessage(MessageUtil.colorize("&7"));
                player.sendMessage(MessageUtil.colorize("&cSelection is partly in claim and partly in wilderness!"));
                player.sendMessage(MessageUtil.colorize("&cYou cannot create a claim or subclaim here."));
                player.sendMessage(MessageUtil.colorize("&7"));
                player.sendMessage(MessageUtil.colorize("&7Options:"));
                player.sendMessage(MessageUtil.colorize("&e1. &7Make selection fully INSIDE claim → use /csubclaim"));
                player.sendMessage(MessageUtil.colorize("&e2. &7Make selection fully OUTSIDE all claims → use /cregioncreate"));
                return true;
            }
            case CLEAR -> {
                // No overlap, proceed normally
            }
        }

        RegionCreateGUI gui = new RegionCreateGUI(plugin, player);
        gui.open();

        return true;
    }
}
