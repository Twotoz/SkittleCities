package twotoz.skittleCities.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import twotoz.skittleCities.SkittleCities;
import twotoz.skittleCities.utils.MessageUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class CommandBlockListener implements Listener {
    private final SkittleCities plugin;
    private final Set<UUID> bypassEnabled;

    public CommandBlockListener(SkittleCities plugin) {
        this.plugin = plugin;
        this.bypassEnabled = new HashSet<>();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String worldName = plugin.getConfig().getString("world-name");

        // Only block commands if player IS in city world
        if (!player.getWorld().getName().equals(worldName)) {
            return; // Outside city = no restrictions
        }

        // Check if player has bypass enabled
        if (bypassEnabled.contains(player.getUniqueId())) {
            return; // Bypass active, allow all commands
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
            String prefix = plugin.getConfig().getString("messages.prefix");
            player.sendMessage(MessageUtil.colorize(prefix + "&cThis command is not allowed in the city!"));
            
            // Show bypass hint if player has permission
            if (player.hasPermission("skittlecities.commandbypass")) {
                player.sendMessage(MessageUtil.colorize(prefix + "&7Tip: Use &e/citycommandbypass &7to temporarily enable all commands"));
            }
        }
    }

    public boolean toggleBypass(UUID playerId) {
        if (bypassEnabled.contains(playerId)) {
            bypassEnabled.remove(playerId);
            return false; // Disabled
        } else {
            bypassEnabled.add(playerId);
            return true; // Enabled
        }
    }

    public boolean hasBypass(UUID playerId) {
        return bypassEnabled.contains(playerId);
    }

    public void removeBypass(UUID playerId) {
        bypassEnabled.remove(playerId);
    }
}
