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

        // NO WORLD CHECK - command itself checks if in city world below

        String cityWorldName = plugin.getConfig().getString("world-name");
        
        // Check if in city world
        if (!player.getWorld().getName().equals(cityWorldName)) {
            player.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
                "&cYou are not in the city!"));
            return true;
        }

        // Combat cooldown check
        if (plugin.getCombatManager().isInCombat(player.getUniqueId())) {
            int remaining = plugin.getCombatManager().getRemainingCombatTime(player.getUniqueId());
            player.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
                "&cYou are in combat! Wait &e" + remaining + " &cseconds before teleporting."));
            return true;
        }

        // Teleport to configured leave-city spawn (or server spawn as fallback)
        Location spawn;
        
        if (plugin.getConfig().contains("leave-city-spawn.world")) {
            // Use configured leave-city spawn
            String worldName = plugin.getConfig().getString("leave-city-spawn.world");
            org.bukkit.World world = plugin.getServer().getWorld(worldName);
            
            if (world != null) {
                double x = plugin.getConfig().getDouble("leave-city-spawn.x");
                double y = plugin.getConfig().getDouble("leave-city-spawn.y");
                double z = plugin.getConfig().getDouble("leave-city-spawn.z");
                float yaw = (float) plugin.getConfig().getDouble("leave-city-spawn.yaw");
                float pitch = (float) plugin.getConfig().getDouble("leave-city-spawn.pitch");
                
                spawn = new Location(world, x, y, z, yaw, pitch);
            } else {
                // World not found - fallback to server spawn
                spawn = plugin.getServer().getWorlds().get(0).getSpawnLocation();
            }
        } else {
            // No leave-city spawn configured - use server spawn
            spawn = plugin.getServer().getWorlds().get(0).getSpawnLocation();
        }
        
        player.teleport(spawn);
        
        player.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
            "&aYou left the city!"));

        return true;
    }
}
