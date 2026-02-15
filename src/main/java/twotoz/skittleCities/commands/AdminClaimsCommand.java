package twotoz.skittleCities.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import twotoz.skittleCities.SkittleCities;
import twotoz.skittleCities.data.Region;
import twotoz.skittleCities.gui.AdminClaimsGUI;
import twotoz.skittleCities.gui.AdminEditClaimGUI;
import twotoz.skittleCities.utils.MessageUtil;

public class AdminClaimsCommand implements CommandExecutor {
    private final SkittleCities plugin;

    public AdminClaimsCommand(SkittleCities plugin) {
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

        // Check if player is standing in a claim
        Region currentRegion = plugin.getRegionManager().getRegionAt(player.getLocation());
        
        if (currentRegion != null) {
            // Player is in a claim - open edit GUI directly
            AdminEditClaimGUI editGUI = new AdminEditClaimGUI(plugin, player, currentRegion);
            editGUI.open();
        } else {
            // Player is not in a claim - open claims list
            AdminClaimsGUI gui = new AdminClaimsGUI(plugin, player);
            gui.open();
        }

        return true;
    }
}
