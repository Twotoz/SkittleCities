package twotoz.skittleCities.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import twotoz.skittleCities.SkittleCities;
import twotoz.skittleCities.utils.MessageUtil;

public class CityCommandBypassCommand implements CommandExecutor {
    private final SkittleCities plugin;

    public CityCommandBypassCommand(SkittleCities plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtil.send(sender, plugin.getConfig(), "player-only");
            return true;
        }

        if (!player.hasPermission("skittlecities.commandbypass")) {
            MessageUtil.send(player, plugin.getConfig(), "no-permission");
            return true;
        }

        boolean enabled = plugin.getCommandBlockListener().toggleBypass(player.getUniqueId());
        
        String prefix = plugin.getConfig().getString("messages.prefix");
        if (enabled) {
            player.sendMessage(MessageUtil.colorize(prefix + "&aCommand bypass enabled! All commands are now allowed in the city."));
            player.sendMessage(MessageUtil.colorize(prefix + "&7Use &e/citycommandbypass &7again to disable."));
        } else {
            player.sendMessage(MessageUtil.colorize(prefix + "&cCommand bypass disabled! Command restrictions restored."));
        }

        return true;
    }
}
