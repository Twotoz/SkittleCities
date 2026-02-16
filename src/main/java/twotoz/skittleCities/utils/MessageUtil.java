package twotoz.skittleCities.utils;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

public class MessageUtil {
    
    public static void send(CommandSender sender, FileConfiguration config, String path) {
        send(sender, config, path, new String[0], new String[0]);
    }
    
    public static void send(CommandSender sender, FileConfiguration config, String path, String[] placeholders, String[] values) {
        String prefix = colorize(config.getString("messages.prefix", "&6[SkittleCities]&r "));
        String message = config.getString("messages." + path, path);
        
        for (int i = 0; i < placeholders.length && i < values.length; i++) {
            message = message.replace(placeholders[i], values[i]);
        }
        
        sender.sendMessage(prefix + colorize(message));
    }
    
    public static String colorize(String message) {
        return message.replace("&", "§");
    }
    
    /**
     * Send protection message with bypass hint if player has permission
     */
    public static void sendProtected(org.bukkit.entity.Player player, FileConfiguration config, String bypassPermission, String bypassCommand) {
        send(player, config, "protected");
        
        // Show bypass hint if player has permission
        if (player.hasPermission(bypassPermission)) {
            String prefix = colorize(config.getString("messages.prefix", "&6[SkittleCities]&r "));
            player.sendMessage(prefix + colorize("&7Tip: Use &e" + bypassCommand + " &7to bypass protection"));
        }
    }
    
    /**
     * Check if player is in the configured world
     * ALL COMMANDS must use this - even for admins!
     * @return true if in correct world, false otherwise (sends error message)
     */
    public static boolean checkWorld(org.bukkit.entity.Player player, FileConfiguration config) {
        String worldName = config.getString("world-name", "world");
        if (!player.getWorld().getName().equals(worldName)) {
            send(player, config, "wrong-world");
            return false;
        }
        return true;
    }
}
