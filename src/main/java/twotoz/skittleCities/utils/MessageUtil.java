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
}
