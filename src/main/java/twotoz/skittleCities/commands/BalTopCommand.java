package twotoz.skittleCities.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import twotoz.skittleCities.SkittleCities;
import twotoz.skittleCities.utils.MessageUtil;

import java.util.*;
import java.util.stream.Collectors;

public class BalTopCommand implements CommandExecutor {
    private final SkittleCities plugin;

    public BalTopCommand(SkittleCities plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Parse limit (default 10)
        int limit = 10;
        if (args.length > 0) {
            try {
                limit = Integer.parseInt(args[0]);
                if (limit < 1 || limit > 50) {
                    sender.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
                        "&cLimit must be between 1 and 50!"));
                    return true;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
                    "&cInvalid number: &e" + args[0]));
                return true;
            }
        }

        // Get all balances from database
        Map<UUID, Double> allBalances = plugin.getDatabaseManager().getAllBalances();
        
        if (allBalances.isEmpty()) {
            sender.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
                "&cNo balance data found!"));
            return true;
        }

        // Sort by balance (highest first)
        List<Map.Entry<UUID, Double>> sortedBalances = allBalances.entrySet().stream()
            .sorted(Map.Entry.<UUID, Double>comparingByValue().reversed())
            .limit(limit)
            .collect(Collectors.toList());

        // Header
        sender.sendMessage(MessageUtil.colorize("&7&m                                       "));
        sender.sendMessage(MessageUtil.colorize("&6&lBalance Top " + limit));
        sender.sendMessage(MessageUtil.colorize("&7&m                                       "));

        // List entries
        int rank = 1;
        for (Map.Entry<UUID, Double> entry : sortedBalances) {
            UUID uuid = entry.getKey();
            double balance = entry.getValue();
            
            // Get player name (online or offline)
            String playerName = getPlayerName(uuid);
            
            // Format rank with colors
            String rankColor = switch (rank) {
                case 1 -> "&6"; // Gold
                case 2 -> "&7"; // Silver
                case 3 -> "&c"; // Bronze
                default -> "&e"; // Yellow
            };
            
            String rankSymbol = switch (rank) {
                case 1 -> "👑"; // Crown
                case 2 -> "🥈"; // Silver medal
                case 3 -> "🥉"; // Bronze medal
                default -> "";
            };
            
            sender.sendMessage(MessageUtil.colorize(
                rankColor + "#" + rank + " " + rankSymbol + " &f" + playerName + 
                " &7- &6$" + String.format("%,.2f", balance)
            ));
            
            rank++;
        }

        sender.sendMessage(MessageUtil.colorize("&7&m                                       "));
        
        // Show sender's rank if they're a player and not in top list
        if (sender instanceof Player player) {
            boolean inTopList = sortedBalances.stream()
                .anyMatch(entry -> entry.getKey().equals(player.getUniqueId()));
            
            if (!inTopList) {
                int playerRank = getPlayerRank(player.getUniqueId(), allBalances);
                double playerBalance = plugin.getEconomyManager().getBalance(player.getUniqueId());
                
                sender.sendMessage(MessageUtil.colorize(
                    "&7Your rank: &e#" + playerRank + " &7- &6$" + String.format("%,.2f", playerBalance)
                ));
            }
        }

        return true;
    }

    /**
     * Get player name from UUID
     */
    private String getPlayerName(UUID uuid) {
        // Try online player first
        Player player = plugin.getServer().getPlayer(uuid);
        if (player != null) {
            return player.getName();
        }
        
        // Try offline player
        @SuppressWarnings("deprecation")
        org.bukkit.OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(uuid);
        
        String name = offlinePlayer.getName();
        return name != null ? name : "Unknown";
    }

    /**
     * Get player's rank in the leaderboard
     */
    private int getPlayerRank(UUID uuid, Map<UUID, Double> allBalances) {
        List<Map.Entry<UUID, Double>> sorted = allBalances.entrySet().stream()
            .sorted(Map.Entry.<UUID, Double>comparingByValue().reversed())
            .collect(Collectors.toList());
        
        for (int i = 0; i < sorted.size(); i++) {
            if (sorted.get(i).getKey().equals(uuid)) {
                return i + 1;
            }
        }
        
        return -1; // Not found
    }
}
