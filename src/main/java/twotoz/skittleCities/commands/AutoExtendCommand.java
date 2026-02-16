package twotoz.skittleCities.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import twotoz.skittleCities.SkittleCities;
import twotoz.skittleCities.data.Region;
import twotoz.skittleCities.utils.MessageUtil;

public class AutoExtendCommand implements CommandExecutor {
    private final SkittleCities plugin;

    public AutoExtendCommand(SkittleCities plugin) {
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

        Region region = plugin.getRegionManager().getRegionAt(player.getLocation());
        
        if (region == null) {
            MessageUtil.send(player, plugin.getConfig(), "not-in-claim");
            return true;
        }

        if (region.getType() != Region.RegionType.FOR_HIRE) {
            player.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
                "&cAuto-extend is only available for leased regions."));
            return true;
        }

        if (region.getOwner() == null || !region.getOwner().equals(player.getUniqueId())) {
            MessageUtil.send(player, plugin.getConfig(), "not-claim-owner");
            return true;
        }

        // Toggle auto-extend
        region.setAutoExtend(!region.isAutoExtend());
        plugin.getRegionManager().updateRegion(region);

        player.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
            (region.isAutoExtend() ? 
                "&aAuto-extend enabled. Your lease will automatically renew if you have sufficient funds." : 
                "&cAuto-extend disabled. Your lease will expire when the time runs out.")));

        return true;
    }
}
