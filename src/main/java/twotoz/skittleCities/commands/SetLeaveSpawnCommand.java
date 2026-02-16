package twotoz.skittleCities.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import twotoz.skittleCities.SkittleCities;
import twotoz.skittleCities.utils.MessageUtil;

public class SetLeaveSpawnCommand implements CommandExecutor {
    private final SkittleCities plugin;

    public SetLeaveSpawnCommand(SkittleCities plugin) {
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

        // Save current location as leave-city spawn
        plugin.getConfig().set("leave-city-spawn.world", player.getWorld().getName());
        plugin.getConfig().set("leave-city-spawn.x", player.getLocation().getX());
        plugin.getConfig().set("leave-city-spawn.y", player.getLocation().getY());
        plugin.getConfig().set("leave-city-spawn.z", player.getLocation().getZ());
        plugin.getConfig().set("leave-city-spawn.yaw", player.getLocation().getYaw());
        plugin.getConfig().set("leave-city-spawn.pitch", player.getLocation().getPitch());
        plugin.saveConfig();

        player.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
            "&aLeave-city spawn set to your current location!"));
        player.sendMessage(MessageUtil.colorize("&7World: &e" + player.getWorld().getName()));
        player.sendMessage(MessageUtil.colorize("&7Location: &e" + 
            String.format("%.1f, %.1f, %.1f", 
                player.getLocation().getX(),
                player.getLocation().getY(),
                player.getLocation().getZ())));
        player.sendMessage(MessageUtil.colorize("&7Players using &e/leavecity &7will teleport here!"));

        return true;
    }
}
