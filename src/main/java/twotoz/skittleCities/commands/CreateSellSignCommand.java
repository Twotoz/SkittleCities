package twotoz.skittleCities.commands;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
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

public class CreateSellSignCommand implements CommandExecutor, TabCompleter {
    private final SkittleCities plugin;

    public CreateSellSignCommand(SkittleCities plugin) {
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

        if (args.length != 2) {
            player.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
                "&cUsage: /csellsign <material> <price>"));
            player.sendMessage(MessageUtil.colorize("&7Example: /csellsign COOKED_PORKCHOP 5.00"));
            return true;
        }

        // Get material
        Material material;
        try {
            material = Material.valueOf(args[0].toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
                "&cInvalid material: &e" + args[0]));
            player.sendMessage(MessageUtil.colorize("&7Use tab completion to see valid materials."));
            return true;
        }

        // Get price
        double price;
        try {
            price = Double.parseDouble(args[1]);
            if (price <= 0) {
                player.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
                    "&cPrice must be greater than 0!"));
                return true;
            }
        } catch (NumberFormatException e) {
            player.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
                "&cInvalid price: &e" + args[1]));
            return true;
        }

        // Get block player is looking at
        Block targetBlock = player.getTargetBlockExact(5);
        
        if (targetBlock == null || !(targetBlock.getState() instanceof Sign)) {
            player.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
                "&cYou must be looking at a sign!"));
            return true;
        }

        Sign sign = (Sign) targetBlock.getState();
        
        // Create sell sign (BUY sign - player sells TO sign)
        plugin.getSellSignManager().createSellSign(sign, material, price, true);
        
        player.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
            "&aCreated [BUY] sign for &e" + material.name().toLowerCase().replace("_", " ") + 
            " &aat &6$" + String.format("%.2f", price) + " &aeach!"));
        player.sendMessage(MessageUtil.colorize("&7Players can right-click to sell their items!"));

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            // Common sellable items
            return Arrays.stream(Material.values())
                .filter(Material::isItem)
                .map(Material::name)
                .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                .limit(50)
                .collect(Collectors.toList());
        } else if (args.length == 2) {
            return Arrays.asList("1.00", "5.00", "10.00", "25.00", "50.00");
        }
        return new ArrayList<>();
    }
}
