package twotoz.skittleCities.commands;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import twotoz.skittleCities.SkittleCities;
import twotoz.skittleCities.utils.MessageUtil;

public class CityCommand implements CommandExecutor {
    private final SkittleCities plugin;

    public CityCommand(SkittleCities plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtil.send(sender, plugin.getConfig(), "player-only");
            return true;
        }

        String worldName = plugin.getConfig().getString("world-name");
        World cityWorld = Bukkit.getWorld(worldName);

        if (cityWorld == null) {
            player.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
                "&cCity world not found!"));
            return true;
        }

        // Teleport to city spawn
        double x = plugin.getConfig().getDouble("city-spawn.x");
        double y = plugin.getConfig().getDouble("city-spawn.y");
        double z = plugin.getConfig().getDouble("city-spawn.z");
        float yaw = (float) plugin.getConfig().getDouble("city-spawn.yaw");
        float pitch = (float) plugin.getConfig().getDouble("city-spawn.pitch");

        org.bukkit.Location spawn = new org.bukkit.Location(cityWorld, x, y, z, yaw, pitch);
        player.teleport(spawn);
        
        player.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
            "&aWelcome to the city!"));

        return true;
    }
}
