package twotoz.skittleCities.managers;

import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import twotoz.skittleCities.SkittleCities;
import twotoz.skittleCities.utils.MessageUtil;

public class SellSignManager {
    private final SkittleCities plugin;
    
    public SellSignManager(SkittleCities plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Check if sign is a valid sell OR buy sign
     */
    public boolean isSellSign(Sign sign) {
        if (sign.getLine(0).isEmpty()) return false;
        String line0 = org.bukkit.ChatColor.stripColor(sign.getLine(0));
        return line0.equalsIgnoreCase("[SELL]") || line0.equalsIgnoreCase("[BUY]");
    }
    
    /**
     * Create a sign (SELL or BUY)
     * @param sign The sign block
     * @param material The material to trade
     * @param price Price per item
     * @param isBuySign true = [BUY] sign (player sells), false = [SELL] sign (player buys)
     */
    public void createSign(Sign sign, Material material, double price, boolean isBuySign) {
        String signType = isBuySign ? "[BUY]" : "[SELL]";
        String statLabel = isBuySign ? "Sold" : "Bought";
        
        sign.setLine(0, MessageUtil.colorize("&9&l" + signType));
        sign.setLine(1, MessageUtil.colorize("&e" + material.name()));
        sign.setLine(2, MessageUtil.colorize("&6$" + String.format("%.2f", price) + " &7each"));
        sign.setLine(3, MessageUtil.colorize("&7" + statLabel + ": &e0"));
        sign.update();
    }
    
    /**
     * Get material from sign
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
     * Get price per item from sign
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
     * Handle player clicking on sign
     */
    public void handleSell(Player player, Sign sign) {
        String line0 = org.bukkit.ChatColor.stripColor(sign.getLine(0));
        
        if (line0.equalsIgnoreCase("[SELL]")) {
            // [SELL] sign = Player BUYS from server
            handlePlayerBuy(player, sign);
        } else if (line0.equalsIgnoreCase("[BUY]")) {
            // [BUY] sign = Player SELLS to server
            handlePlayerSell(player, sign);
        }
    }
    
    /**
     * [SELL] Sign - Player BUYS items FROM server
     * Player pays money, gets items
     */
    private void handlePlayerBuy(Player player, Sign sign) {
        Material material = getMaterial(sign);
        if (material == null) {
            player.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
                "&cInvalid sign!"));
            return;
        }
        
        double pricePerItem = getPrice(sign);
        if (pricePerItem <= 0) {
            player.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
                "&cInvalid price on sign!"));
            return;
        }
        
        // Calculate how many items player can afford
        double balance = plugin.getEconomyManager().getBalance(player.getUniqueId());
        int maxAffordable = (int) (balance / pricePerItem);
        
        if (maxAffordable == 0) {
            player.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
                "&cYou can't afford any &e" + material.name().toLowerCase().replace("_", " ") + "&c!"));
            player.sendMessage(MessageUtil.colorize("&7Price: &6$" + String.format("%.2f", pricePerItem) + 
                " &7each | Your balance: &6$" + String.format("%.2f", balance)));
            return;
        }
        
        // Check inventory space
        int availableSlots = 0;
        for (ItemStack item : player.getInventory().getStorageContents()) {
            if (item == null || item.getType() == Material.AIR) {
                availableSlots++;
            } else if (item.getType() == material && item.getAmount() < item.getMaxStackSize()) {
                availableSlots++;
            }
        }
        
        if (availableSlots == 0) {
            player.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
                "&cYour inventory is full!"));
            return;
        }
        
        // Calculate max items based on inventory space (64 per slot)
        int maxBySpace = availableSlots * 64;
        
        // Actual amount to buy (min of affordable and inventory space)
        int amountToBuy = Math.min(maxAffordable, maxBySpace);
        
        // Calculate total cost
        double totalCost = amountToBuy * pricePerItem;
        
        // Remove money
        plugin.getEconomyManager().removeBalance(player.getUniqueId(), totalCost);
        
        // Give items
        ItemStack items = new ItemStack(material, amountToBuy);
        player.getInventory().addItem(items);
        
        // Send confirmation
        player.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
            "&aBought &e" + amountToBuy + "x " + material.name().toLowerCase().replace("_", " ") + 
            " &afor &6$" + String.format("%.2f", totalCost)));
        
        // Update sign stats (line 3)
        updateSignStats(sign, amountToBuy, "Bought");
    }
    
    /**
     * [BUY] Sign - Player SELLS items TO server
     * Player gives items, gets money
     */
    private void handlePlayerSell(Player player, Sign sign) {
        Material material = getMaterial(sign);
        if (material == null) {
            player.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
                "&cInvalid sign!"));
            return;
        }
        
        double pricePerItem = getPrice(sign);
        if (pricePerItem <= 0) {
            player.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
                "&cInvalid price on sign!"));
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
        updateSignStats(sign, removed, "Sold");
    }
    
    /**
     * Update sign statistics
     */
    private void updateSignStats(Sign sign, int amount, String action) {
        try {
            String statsLine = org.bukkit.ChatColor.stripColor(sign.getLine(3));
            int total = 0;
            if (statsLine.contains(":")) {
                String[] parts = statsLine.split(":");
                if (parts.length > 1) {
                    total = Integer.parseInt(parts[1].trim());
                }
            }
            total += amount;
            sign.setLine(3, MessageUtil.colorize("&7" + action + ": &e" + total));
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
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material) {
                int toRemove = Math.min(remaining, item.getAmount());
                item.setAmount(item.getAmount() - toRemove);
                remaining -= toRemove;
                
                if (remaining == 0) break;
            }
        }
        return amount - remaining;
    }
}
