package twotoz.skittleCities.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import twotoz.skittleCities.SkittleCities;
import twotoz.skittleCities.utils.MessageUtil;

import java.util.List;

public class CommandBlockListener implements Listener {
    private final SkittleCities plugin;

    public CommandBlockListener(SkittleCities plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String worldName = plugin.getConfig().getString("world-name");

        // Only block commands if player IS in city world
        if (!player.getWorld().getName().equals(worldName)) {
            return; // Outside city = no restrictions
        }

        // Get command (remove the /)
        String message = event.getMessage();
        String command = message.substring(1).split(" ")[0].toLowerCase();

        // Get allowed commands IN city
        List<String> allowedCommands = plugin.getConfig().getStringList("allowed-commands-in-city");

        // Check if command is allowed
        boolean allowed = false;
        for (String allowedCmd : allowedCommands) {
            if (command.equals(allowedCmd.toLowerCase())) {
                allowed = true;
                break;
            }
        }

        if (!allowed) {
            event.setCancelled(true);
            player.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
                "&cThis command is not allowed in the city!"));
        }
    }
}
