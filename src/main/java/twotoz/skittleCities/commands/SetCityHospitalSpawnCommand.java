package twotoz.skittleCities.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import twotoz.skittleCities.SkittleCities;
import twotoz.skittleCities.utils.MessageUtil;

public class SetCityHospitalSpawnCommand implements CommandExecutor {
    private final SkittleCities plugin;

    public SetCityHospitalSpawnCommand(SkittleCities plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Player only.");
            return true;
        }

        if (!player.hasPermission("skittlecities.admin")) {
            player.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") +
                "&cNo permission."));
            return true;
        }

        org.bukkit.Location loc = player.getLocation();
        plugin.getConfig().set("hospital-spawn.configured", true);
        plugin.getConfig().set("hospital-spawn.world", loc.getWorld().getName());
        plugin.getConfig().set("hospital-spawn.x", loc.getX());
        plugin.getConfig().set("hospital-spawn.y", loc.getY());
        plugin.getConfig().set("hospital-spawn.z", loc.getZ());
        plugin.getConfig().set("hospital-spawn.yaw", loc.getYaw());
        plugin.getConfig().set("hospital-spawn.pitch", loc.getPitch());
        plugin.saveConfig();

        player.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") +
            "&aHospital spawn set to your location!"));
        return true;
    }
}
