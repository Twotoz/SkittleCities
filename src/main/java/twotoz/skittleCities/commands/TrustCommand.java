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
import java.util.stream.Collectors;

public class TrustCommand implements CommandExecutor, TabCompleter {
    private final SkittleCities plugin;

    public TrustCommand(SkittleCities plugin) {
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

        if (args.length != 1) {
            player.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
                "&cUsage: /ctrust <player>"));
            return true;
        }

        Region region = plugin.getRegionManager().getRegionAt(player.getLocation());
        
        if (region == null) {
            MessageUtil.send(player, plugin.getConfig(), "not-in-claim");
            return true;
        }

        // Check if player is owner OR has admin permission
        boolean isOwner = region.getOwner() != null && region.getOwner().equals(player.getUniqueId());
        boolean hasAdminPerm = player.hasPermission("skittlecities.admintrust");
        
        if (!isOwner && !hasAdminPerm) {
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

        // Prevent self-trust
        if (target.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
                "&cYou can't trust yourself - you already own this claim!"));
            return true;
        }

        String targetName = target.getName() != null ? target.getName() : args[0];
        
        if (plugin.getTrustManager().trustPlayer(region, target.getUniqueId())) {
            MessageUtil.send(player, plugin.getConfig(), "player-trusted",
                new String[]{"%player%"},
                new String[]{targetName});
            player.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
                "&7Use &e/cuntrust " + targetName + " &7to remove access"));
        } else {
            MessageUtil.send(player, plugin.getConfig(), "already-trusted",
                new String[]{"%player%"},
                new String[]{targetName});
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return plugin.getServer().getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
