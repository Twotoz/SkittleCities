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

        double balance = plugin.getEconomyManager().getBalance(player.getUniqueId());
        
        MessageUtil.send(player, plugin.getConfig(), "balance", 
            new String[]{"%balance%"}, 
            new String[]{String.format("%.2f", balance)});
        
        return true;
    }
}
