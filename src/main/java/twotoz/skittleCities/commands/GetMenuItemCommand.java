package twotoz.skittleCities.commands;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import twotoz.skittleCities.SkittleCities;
import twotoz.skittleCities.utils.MessageUtil;

import java.util.Arrays;

public class GetMenuItemCommand implements CommandExecutor {
    private final SkittleCities plugin;

    public GetMenuItemCommand(SkittleCities plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtil.send(sender, plugin.getConfig(), "player-only");
            return true;
        }

        // Create menu item
        ItemStack menuItem = new ItemStack(Material.COMPASS);
        ItemMeta meta = menuItem.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(MessageUtil.colorize("§6§lCity Menu"));
            meta.setLore(Arrays.asList(
                MessageUtil.colorize("&7Click to open the city menu"),
                MessageUtil.colorize("&8Cannot be dropped or destroyed")
            ));
            menuItem.setItemMeta(meta);
        }
        
        // Give to player in slot 0
        player.getInventory().setItem(0, menuItem);
        
        player.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
            "&aMenu item given to slot 1 (first hotbar slot)!"));

        return true;
    }
}
