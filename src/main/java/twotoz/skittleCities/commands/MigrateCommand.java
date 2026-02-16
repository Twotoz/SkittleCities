package twotoz.skittleCities.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import twotoz.skittleCities.SkittleCities;
import twotoz.skittleCities.data.Region;
import twotoz.skittleCities.utils.MessageUtil;

import java.util.Map;

public class MigrateCommand implements CommandExecutor {
    private final SkittleCities plugin;

    public MigrateCommand(SkittleCities plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("skittlecities.admin")) {
            MessageUtil.send(sender, plugin.getConfig(), "no-permission");
            return true;
        }

        sender.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
            "&eStarting database migration..."));

        int totalRegions = 0;
        int updated = 0;

        // Get default flags from config
        Map<String, Object> defaultFlags = plugin.getConfig().getConfigurationSection("default-claim-flags").getValues(false);

        // Check all regions for missing flags
        for (Region region : plugin.getRegionManager().getAllRegions()) {
            boolean needsUpdate = false;
            
            for (Map.Entry<String, Object> entry : defaultFlags.entrySet()) {
                String flagName = entry.getKey();
                
                // If region doesn't have this flag, add it
                if (!region.getFlags().containsKey(flagName)) {
                    region.getFlags().put(flagName, (Boolean) entry.getValue());
                    needsUpdate = true;
                }
            }
            
            if (needsUpdate) {
                plugin.getRegionManager().updateRegion(region);
                updated++;
            }
            
            totalRegions++;
        }

        sender.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
            "&aDatabase migration complete!"));
        sender.sendMessage(MessageUtil.colorize("&7Total regions: &e" + totalRegions));
        sender.sendMessage(MessageUtil.colorize("&7Updated with missing flags: &e" + updated));

        return true;
    }
}
