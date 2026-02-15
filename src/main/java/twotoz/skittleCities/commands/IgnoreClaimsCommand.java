package twotoz.skittleCities.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import twotoz.skittleCities.SkittleCities;
import twotoz.skittleCities.utils.MessageUtil;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class IgnoreClaimsCommand implements CommandExecutor {
    private final SkittleCities plugin;
    private final Set<UUID> bypassPlayers;

    public IgnoreClaimsCommand(SkittleCities plugin) {
        this.plugin = plugin;
        this.bypassPlayers = new HashSet<>();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtil.send(sender, plugin.getConfig(), "player-only");
            return true;
        }

        if (!player.hasPermission("skittlecities.admin")) {
            MessageUtil.send(player, plugin.getConfig(), "no-permission");
            return true;
        }

        UUID playerId = player.getUniqueId();

        if (bypassPlayers.contains(playerId)) {
            bypassPlayers.remove(playerId);
            MessageUtil.send(player, plugin.getConfig(), "ignore-claims-disabled");
        } else {
            bypassPlayers.add(playerId);
            MessageUtil.send(player, plugin.getConfig(), "ignore-claims-enabled");
        }

        return true;
    }

    public boolean isBypassing(UUID playerId) {
        return bypassPlayers.contains(playerId);
    }

    public void cleanup(Player player) {
        bypassPlayers.remove(player.getUniqueId());
    }
}
