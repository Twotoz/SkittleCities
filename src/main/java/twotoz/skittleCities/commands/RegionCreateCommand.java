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

        RegionCreateGUI gui = new RegionCreateGUI(plugin, player);
        gui.open();

        return true;
    }
}
