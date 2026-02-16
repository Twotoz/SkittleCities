package twotoz.skittleCities.managers;

import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import twotoz.skittleCities.SkittleCities;
import twotoz.skittleCities.utils.MessageUtil;

import java.util.HashMap;
import java.util.Map;

public class SellSignManager {
    private final SkittleCities plugin;
    
    public SellSignManager(SkittleCities plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Check if sign is a valid sell sign
     */
    public boolean isSellSign(Sign sign) {
        if (sign.getLine(0).isEmpty()) return false;
        String line0 = org.bukkit.ChatColor.stripColor(sign.getLine(0));
        return line0.equalsIgnoreCase("[SELL]") || line0.equalsIgnoreCase("[BUY]");
    }
    
    /**
     * Check if this is a BUY sign (player sells TO sign)
     */
    public boolean isBuySign(Sign sign) {
        if (sign.getLine(0).isEmpty()) return false;
        String line0 = org.bukkit.ChatColor.stripColor(sign.getLine(0));
        return line0.equalsIgnoreCase("[BUY]");
    }
    
    /**
     * Get material from sell sign
     */
    public Material getMaterial(Sign sign) {
        try {
            String materialName = org.bukkit.ChatColor.stripColor(sign.getLine(1)).toUpperCase();
            return Material.valueOf(materialName);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    /**
     * Get price per item from sell sign
     */
    public double getPrice(Sign sign) {
        try {
            String priceLine = org.bukkit.ChatColor.stripColor(sign.getLine(2));
            // Remove $, "each", spaces
            priceLine = priceLine.replace("$", "").replace("each", "").trim();
            return Double.parseDouble(priceLine);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
    
    /**
     * Handle player selling items to sign
     */
    public void handleSell(Player player, Sign sign) {
        Material material = getMaterial(sign);
        if (material == null) {
            player.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
                "&cInvalid sell sign!"));
            return;
        }
        
        double pricePerItem = getPrice(sign);
        if (pricePerItem <= 0) {
            player.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
                "&cInvalid price on sell sign!"));
            return;
        }
        
        // Count how many items player has
        int itemCount = countItems(player, material);
        
        if (itemCount == 0) {
            player.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
                "&cYou don't have any &e" + material.name().toLowerCase().replace("_", " ") + " &cto sell!"));
            return;
        }
        
        // Remove items from inventory
        int removed = removeItems(player, material, itemCount);
        
        // Calculate earnings
        double earnings = removed * pricePerItem;
        
        // Give money
        plugin.getEconomyManager().addBalance(player.getUniqueId(), earnings);
        
        // Send confirmation
        player.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
            "&aSold &e" + removed + "x " + material.name().toLowerCase().replace("_", " ") + 
            " &afor &6$" + String.format("%.2f", earnings)));
        
        // Update sign stats (line 3)
        try {
            String statsLine = org.bukkit.ChatColor.stripColor(sign.getLine(3));
            int totalSold = 0;
            if (statsLine.contains("Sold:")) {
                String[] parts = statsLine.split(":");
                if (parts.length > 1) {
                    totalSold = Integer.parseInt(parts[1].trim());
                }
            }
            totalSold += removed;
            sign.setLine(3, MessageUtil.colorize("&7Sold: &e" + totalSold));
            sign.update();
        } catch (Exception e) {
            // Ignore stats update errors
        }
    }
    
    /**
     * Count items in player inventory
     */
    private int countItems(Player player, Material material) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material) {
                count += item.getAmount();
            }
        }
        return count;
    }
    
    /**
     * Remove items from player inventory
     */
    private int removeItems(Player player, Material material, int amount) {
        int remaining = amount;
        ItemStack[] contents = player.getInventory().getContents();
        
        for (int i = 0; i < contents.length; i++) {
            if (contents[i] != null && contents[i].getType() == material) {
                int stackAmount = contents[i].getAmount();
                
                if (stackAmount <= remaining) {
                    // Remove entire stack
                    remaining -= stackAmount;
                    player.getInventory().setItem(i, null);
                } else {
                    // Remove partial stack
                    contents[i].setAmount(stackAmount - remaining);
                    remaining = 0;
                }
                
                if (remaining == 0) break;
            }
        }
        
        player.updateInventory();
        return amount - remaining;
    }
    
    /**
     * Create sell/buy sign (admin command)
     */
    public boolean createSellSign(Sign sign, Material material, double price, boolean isBuy) {
        if (isBuy) {
            sign.setLine(0, MessageUtil.colorize("&9&l[BUY]"));
        } else {
            sign.setLine(0, MessageUtil.colorize("&9&l[SELL]"));
        }
        sign.setLine(1, MessageUtil.colorize("&e" + material.name()));
        sign.setLine(2, MessageUtil.colorize("&6$" + String.format("%.2f", price) + " &7each"));
        sign.setLine(3, MessageUtil.colorize("&7Sold: &e0"));
        sign.update();
        return true;
    }
}
