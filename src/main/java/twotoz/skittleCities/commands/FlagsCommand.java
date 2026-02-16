package twotoz.skittleCities.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import twotoz.skittleCities.SkittleCities;
import twotoz.skittleCities.data.Region;
import twotoz.skittleCities.gui.FlagsGUI;
import twotoz.skittleCities.utils.MessageUtil;

public class FlagsCommand implements CommandExecutor {
    private final SkittleCities plugin;

    public FlagsCommand(SkittleCities plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtil.send(sender, plugin.getConfig(), "player-only");
            return true;
        }

        // ADMIN ONLY - only admins can modify flags
        if (!player.hasPermission("skittlecities.admin")) {
            MessageUtil.send(player, plugin.getConfig(), "no-permission");
            return true;
        }

        String worldName = plugin.getConfig().getString("world-name");
        if (!player.getWorld().getName().equals(worldName)) {
            MessageUtil.send(player, plugin.getConfig(), "wrong-world");
            return true;
        }

        Region region = plugin.getRegionManager().getRegionAt(player.getLocation());

        FlagsGUI gui = new FlagsGUI(plugin, player, region);
        gui.open();

        return true;
    }
}
