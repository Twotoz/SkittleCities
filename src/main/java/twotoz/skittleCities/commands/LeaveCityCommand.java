package twotoz.skittleCities.commands;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import twotoz.skittleCities.SkittleCities;
import twotoz.skittleCities.utils.MessageUtil;

public class LeaveCityCommand implements CommandExecutor {
    private final SkittleCities plugin;

    public LeaveCityCommand(SkittleCities plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtil.send(sender, plugin.getConfig(), "player-only");
            return true;
        }

        String cityWorldName = plugin.getConfig().getString("world-name");
        
        // Check if in city world
        if (!player.getWorld().getName().equals(cityWorldName)) {
            player.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
                "&cYou are not in the city!"));
            return true;
        }

        // Teleport to server spawn (default world spawn)
        Location spawn = plugin.getServer().getWorlds().get(0).getSpawnLocation();
        player.teleport(spawn);
        
        player.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
            "&aYou left the city!"));

        return true;
    }
}
