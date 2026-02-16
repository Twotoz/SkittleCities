package twotoz.skittleCities.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import twotoz.skittleCities.SkittleCities;
import twotoz.skittleCities.utils.MessageUtil;

public class BalanceCommand implements CommandExecutor {
    private final SkittleCities plugin;

    public BalanceCommand(SkittleCities plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtil.send(sender, plugin.getConfig(), "player-only");
            return true;
        }

        // WORLD CHECK - Must be in configured world (even for admins!)
        if (!MessageUtil.checkWorld(player, plugin.getConfig())) {
            return true;
        }

        // Check if viewing another player's balance
        if (args.length > 0) {
            // Permission check for viewing others
            if (!player.hasPermission("skittlecities.balance.others")) {
                MessageUtil.send(player, plugin.getConfig(), "no-permission");
                return true;
            }
            
            // Support offline players
            @SuppressWarnings("deprecation")
            org.bukkit.OfflinePlayer target = plugin.getServer().getOfflinePlayer(args[0]);
            
            // Check if player has ever played (valid player)
            if (!target.hasPlayedBefore() && !target.isOnline()) {
                player.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
                    "&cPlayer not found. Make sure you typed the name correctly."));
                return true;
            }
            
            String targetName = target.getName() != null ? target.getName() : args[0];
            double balance = plugin.getEconomyManager().getBalance(target.getUniqueId());
            
            player.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
                "&e" + targetName + "'s &7balance: &6$" + String.format("%.2f", balance)));
            return true;
        }

        // Show own balance
        double balance = plugin.getEconomyManager().getBalance(player.getUniqueId());
        
        MessageUtil.send(player, plugin.getConfig(), "balance", 
            new String[]{"%balance%"}, 
            new String[]{String.format("%.2f", balance)});
        
        return true;
    }
}
