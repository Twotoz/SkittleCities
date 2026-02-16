package twotoz.skittleCities.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import twotoz.skittleCities.SkittleCities;
import twotoz.skittleCities.data.Region;
import twotoz.skittleCities.utils.MessageUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class UntrustCommand implements CommandExecutor, TabCompleter {
    private final SkittleCities plugin;

    public UntrustCommand(SkittleCities plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtil.send(sender, plugin.getConfig(), "player-only");
            return true;
        }

        if (!player.hasPermission("skittlecities.trust")) {
            MessageUtil.send(player, plugin.getConfig(), "no-permission");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
                "&cUsage: /cuntrust <player>"));
            return true;
        }

        Region region = plugin.getRegionManager().getRegionAt(player.getLocation());
        
        if (region == null) {
            MessageUtil.send(player, plugin.getConfig(), "not-in-claim");
            return true;
        }

        if (region.getOwner() == null || !region.getOwner().equals(player.getUniqueId())) {
            MessageUtil.send(player, plugin.getConfig(), "not-claim-owner");
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

        // Prevent self-untrust
        if (target.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
                "&cYou can't untrust yourself - you own this claim!"));
            return true;
        }

        String targetName = target.getName() != null ? target.getName() : args[0];
        
        if (plugin.getTrustManager().untrustPlayer(region, target.getUniqueId())) {
            MessageUtil.send(player, plugin.getConfig(), "player-untrusted",
                new String[]{"%player%"},
                new String[]{targetName});
        } else {
            MessageUtil.send(player, plugin.getConfig(), "not-trusted",
                new String[]{"%player%"},
                new String[]{targetName});
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1 && sender instanceof Player player) {
            Region region = plugin.getRegionManager().getRegionAt(player.getLocation());
            if (region != null && region.getOwner() != null && region.getOwner().equals(player.getUniqueId())) {
                return region.getTrustedPlayers().stream()
                    .map(uuid -> plugin.getServer().getOfflinePlayer(uuid).getName())
                    .filter(name -> name != null && name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
            }
        }
        return new ArrayList<>();
    }
}
