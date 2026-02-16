package twotoz.skittleCities.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import twotoz.skittleCities.SkittleCities;
import twotoz.skittleCities.utils.MessageUtil;

public class ReloadCommand implements CommandExecutor {
    private final SkittleCities plugin;

    public ReloadCommand(SkittleCities plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("skittlecities.admin")) {
            MessageUtil.send(sender, plugin.getConfig(), "no-permission");
            return true;
        }

        // Reload config
        plugin.reloadConfig();
        
        sender.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
            "&aConfiguration reloaded successfully!"));
        sender.sendMessage(MessageUtil.colorize("&7World: &e" + plugin.getConfig().getString("world-name")));
        sender.sendMessage(MessageUtil.colorize("&7City spawn updated: &e" + 
            plugin.getConfig().getDouble("city-spawn.x") + ", " +
            plugin.getConfig().getDouble("city-spawn.y") + ", " +
            plugin.getConfig().getDouble("city-spawn.z")));

        return true;
    }
}
