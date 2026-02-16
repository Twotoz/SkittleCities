package twotoz.skittleCities.commands;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import twotoz.skittleCities.SkittleCities;
import twotoz.skittleCities.utils.MessageUtil;

public class SetCitySpawnCommand implements CommandExecutor {
    private final SkittleCities plugin;

    public SetCitySpawnCommand(SkittleCities plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtil.send(sender, plugin.getConfig(), "player-only");
            return true;
        }

        // WORLD CHECK - Must be in configured world (even for admins!)
        if (!MessageUtil.checkWorld(player, plugin.getConfig())) {
            return true;
        }

        if (!player.hasPermission("skittlecities.admin")) {
            MessageUtil.send(player, plugin.getConfig(), "no-permission");
            return true;
        }

        Location loc = player.getLocation();
        
        plugin.getConfig().set("city-spawn.world", loc.getWorld().getName());
        plugin.getConfig().set("city-spawn.x", loc.getX());
        plugin.getConfig().set("city-spawn.y", loc.getY());
        plugin.getConfig().set("city-spawn.z", loc.getZ());
        plugin.getConfig().set("city-spawn.yaw", loc.getYaw());
        plugin.getConfig().set("city-spawn.pitch", loc.getPitch());
        plugin.saveConfig();

        player.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
            "&aCity spawn set to your current location!"));

        return true;
    }
}
