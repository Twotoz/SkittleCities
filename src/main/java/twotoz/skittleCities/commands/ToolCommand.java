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

public class ToolCommand implements CommandExecutor {
    private final SkittleCities plugin;

    public ToolCommand(SkittleCities plugin) {
        this.plugin = plugin;
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

        String toolName = plugin.getConfig().getString("selection-tool", "WOODEN_AXE");
        Material toolMaterial = Material.valueOf(toolName);

        ItemStack tool = new ItemStack(toolMaterial);
        ItemMeta meta = tool.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(MessageUtil.colorize("&6Region Selection Tool"));
            meta.setLore(Arrays.asList(
                MessageUtil.colorize("&7Left click: Set first position"),
                MessageUtil.colorize("&7Right click: Set second position")
            ));
            tool.setItemMeta(meta);
        }

        player.getInventory().addItem(tool);
        player.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
            "&aYou received the region selection tool!"));
        player.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
            "&7Left-click a block for position 1, right-click for position 2"));
        player.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
            "&7Then use &e/cregioncreate &7to create a region"));

        return true;
    }
}
