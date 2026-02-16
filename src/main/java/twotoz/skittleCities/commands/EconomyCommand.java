package twotoz.skittleCities.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import twotoz.skittleCities.SkittleCities;
import twotoz.skittleCities.utils.MessageUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EconomyCommand implements CommandExecutor, TabCompleter {
    private final SkittleCities plugin;

    public EconomyCommand(SkittleCities plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("skittlecities.admin")) {
            MessageUtil.send(sender, plugin.getConfig(), "no-permission");
            return true;
        }

        // WORLD CHECK - Must be in configured world (even for admins!)
        if (sender instanceof Player player) {
            if (!MessageUtil.checkWorld(player, plugin.getConfig())) {
                return true;
            }
        }

        if (args.length < 3) {
            sender.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
                "&cUsage: /ceconomy <give|take|set> <player> <amount>"));
            return true;
        }

        String action = args[0].toLowerCase();
        
        // Support offline players
        @SuppressWarnings("deprecation")
        org.bukkit.OfflinePlayer target = plugin.getServer().getOfflinePlayer(args[1]);
        
        // Check if player has ever played (valid player)
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            sender.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
                "&cPlayer not found. Make sure you typed the name correctly."));
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
                "&cInvalid amount."));
            return true;
        }

        String targetName = target.getName() != null ? target.getName() : args[1];

        switch (action) {
            case "give":
                plugin.getEconomyManager().addBalance(target.getUniqueId(), amount);
                sender.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
                    "&aGave &e$" + amount + "&a to &e" + targetName));
                // Only send message if player is online
                if (target.isOnline() && target.getPlayer() != null) {
                    target.getPlayer().sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
                        "&aYou received &e$" + amount));
                }
                break;

            case "take":
                plugin.getEconomyManager().removeBalance(target.getUniqueId(), amount);
                sender.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
                    "&aTook &e$" + amount + "&a from &e" + targetName));
                // Only send message if player is online
                if (target.isOnline() && target.getPlayer() != null) {
                    target.getPlayer().sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
                        "&c$" + amount + " was removed from your balance"));
                }
                break;

            case "set":
                plugin.getEconomyManager().setBalance(target.getUniqueId(), amount);
                sender.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
                    "&aSet &e" + targetName + "'s&a balance to &e$" + amount));
                // Only send message if player is online
                if (target.isOnline() && target.getPlayer() != null) {
                    target.getPlayer().sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
                        "&aYour balance was set to &e$" + amount));
                }
                break;

            default:
                sender.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
                    "&cUsage: /ceconomy <give|take|set> <player> <amount>"));
                break;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("give", "take", "set").stream()
                .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
        } else if (args.length == 2) {
            return plugin.getServer().getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                .collect(Collectors.toList());
        } else if (args.length == 3) {
            return Arrays.asList("100", "500", "1000", "5000", "10000");
        }
        return new ArrayList<>();
    }
}
