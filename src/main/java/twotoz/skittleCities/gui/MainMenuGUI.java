package twotoz.skittleCities.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import twotoz.skittleCities.SkittleCities;
import twotoz.skittleCities.data.Region;
import twotoz.skittleCities.utils.MessageUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Smart context-aware menu that adapts based on player location
 */
public class MainMenuGUI implements Listener {
    private final SkittleCities plugin;

    public MainMenuGUI(SkittleCities plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Region region = plugin.getRegionManager().getRegionAt(player.getLocation());
        String cityWorld = plugin.getConfig().getString("world-name");
        boolean inCity = player.getWorld().getName().equals(cityWorld);
        
        Inventory inv;
        
        if (region != null && region.getOwner() != null && region.getOwner().equals(player.getUniqueId())) {
            // IN YOUR OWN CLAIM
            inv = createOwnClaimMenu(player, region, inCity);
        } else if (region != null && region.getOwner() != null) {
            // IN SOMEONE ELSE'S CLAIM
            inv = createOtherClaimMenu(player, region, inCity);
        } else {
            // IN WILDERNESS
            inv = createWildernessMenu(player, inCity);
        }
        
        player.openInventory(inv);
    }

    private Inventory createWildernessMenu(Player player, boolean inCity) {
        Inventory inv = plugin.getServer().createInventory(null, 27, MessageUtil.colorize("&6&lMenu &8| &7Wilderness"));
        
        // Balance
        double balance = plugin.getEconomyManager().getBalance(player.getUniqueId());
        inv.setItem(10, createItem(Material.GOLD_INGOT, "&6Your Balance", 
            "&7$" + String.format("%.2f", balance)));
        
        // My Claims
        int claimCount = (int) plugin.getRegionManager().getAllRegions().stream()
            .filter(r -> r.getOwner() != null && r.getOwner().equals(player.getUniqueId()))
            .count();
        inv.setItem(12, createItem(Material.GRASS_BLOCK, "&aMy Claims", 
            "&7You own &e" + claimCount + " &7claim" + (claimCount != 1 ? "s" : ""),
            "",
            "&eClick to view"));
        
        // City / Leave City
        if (inCity) {
            inv.setItem(14, createItem(Material.OAK_DOOR, "&cLeave City", 
                "&7Return to server spawn",
                "",
                "&eClick to leave"));
        } else {
            inv.setItem(14, createItem(Material.EMERALD, "&aGo to City", 
                "&7Teleport to city spawn",
                "",
                "&eClick to teleport"));
        }
        
        // Help
        inv.setItem(16, createItem(Material.BOOK, "&eHelp", 
            "&7View commands and info",
            "",
            "&eClick to open"));
        
        // Admin (if permission)
        if (player.hasPermission("skittlecities.admin")) {
            inv.setItem(20, createItem(Material.COMMAND_BLOCK, "&cAdmin Tools", 
                "&7Manage all claims & economy",
                "",
                "&eClick to open"));
        }
        
        // Close
        inv.setItem(22, createItem(Material.BARRIER, "&cClose", ""));
        
        return inv;
    }

    private Inventory createOwnClaimMenu(Player player, Region region, boolean inCity) {
        Inventory inv = plugin.getServer().createInventory(null, 27, MessageUtil.colorize("&6&lMenu &8| &aYour Claim"));
        
        // Balance
        double balance = plugin.getEconomyManager().getBalance(player.getUniqueId());
        inv.setItem(10, createItem(Material.GOLD_INGOT, "&6Your Balance", 
            "&7$" + String.format("%.2f", balance)));
        
        // Manage Claim
        inv.setItem(11, createItem(Material.WRITABLE_BOOK, "&aManage Claim", 
            "&7Claim: &e" + region.getName(),
            "&7Type: &e" + region.getType(),
            "",
            "&eClick to manage"));
        
        // Trust Player
        inv.setItem(13, createItem(Material.PLAYER_HEAD, "&aTrust Player", 
            "&7Allow others to build",
            "&7Trusted: &e" + region.getTrustedPlayers().size(),
            "",
            "&eUse /ctrust <player>"));
        
        // All Claims
        inv.setItem(15, createItem(Material.GRASS_BLOCK, "&aMy Claims", 
            "&7View all your claims",
            "",
            "&eClick to view"));
        
        // City / Leave City
        if (inCity) {
            inv.setItem(16, createItem(Material.OAK_DOOR, "&cLeave City", 
                "&7Return to server spawn",
                "",
                "&eClick to leave"));
        } else {
            inv.setItem(16, createItem(Material.EMERALD, "&aGo to City", 
                "&7Teleport to city spawn",
                "",
                "&eClick to teleport"));
        }
        
        // Help
        inv.setItem(18, createItem(Material.BOOK, "&eHelp", 
            "&7View commands and info",
            "",
            "&eClick to open"));
        
        // Admin (if permission)
        if (player.hasPermission("skittlecities.admin")) {
            inv.setItem(20, createItem(Material.COMMAND_BLOCK, "&cAdmin Tools", 
                "&7Manage all claims & economy",
                "",
                "&eClick to open"));
        }
        
        // Close
        inv.setItem(22, createItem(Material.BARRIER, "&cClose", ""));
        
        return inv;
    }

    private Inventory createOtherClaimMenu(Player player, Region region, boolean inCity) {
        Inventory inv = plugin.getServer().createInventory(null, 27, MessageUtil.colorize("&6&lMenu &8| &cOther's Claim"));
        
        String ownerName = plugin.getServer().getOfflinePlayer(region.getOwner()).getName();
        if (ownerName == null) ownerName = "Unknown";
        
        // Balance
        double balance = plugin.getEconomyManager().getBalance(player.getUniqueId());
        inv.setItem(10, createItem(Material.GOLD_INGOT, "&6Your Balance", 
            "&7$" + String.format("%.2f", balance)));
        
        // Owner Info
        inv.setItem(12, createItem(Material.PLAYER_HEAD, "&eOwner: &f" + ownerName, 
            "&7Claim: &e" + region.getName(),
            "&7Type: &e" + region.getType(),
            "",
            "&7You are " + (region.getTrustedPlayers().contains(player.getUniqueId()) ? "&atrusted" : "&cnot trusted")));
        
        // My Claims
        int claimCount = (int) plugin.getRegionManager().getAllRegions().stream()
            .filter(r -> r.getOwner() != null && r.getOwner().equals(player.getUniqueId()))
            .count();
        inv.setItem(14, createItem(Material.GRASS_BLOCK, "&aMy Claims", 
            "&7You own &e" + claimCount + " &7claim" + (claimCount != 1 ? "s" : ""),
            "",
            "&eClick to view"));
        
        // City / Leave City
        if (inCity) {
            inv.setItem(16, createItem(Material.OAK_DOOR, "&cLeave City", 
                "&7Return to server spawn",
                "",
                "&eClick to leave"));
        } else {
            inv.setItem(16, createItem(Material.EMERALD, "&aGo to City", 
                "&7Teleport to city spawn",
                "",
                "&eClick to teleport"));
        }
        
        // Help
        inv.setItem(18, createItem(Material.BOOK, "&eHelp", 
            "&7View commands and info",
            "",
            "&eClick to open"));
        
        // Admin (if permission)
        if (player.hasPermission("skittlecities.admin")) {
            inv.setItem(20, createItem(Material.COMMAND_BLOCK, "&cAdmin Tools", 
                "&7Manage all claims & economy",
                "",
                "&eClick to open"));
        }
        
        // Close
        inv.setItem(22, createItem(Material.BARRIER, "&cClose", ""));
        
        return inv;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getView().getTitle() == null) return;
        if (!event.getView().getTitle().contains("Menu")) return;

        event.setCancelled(true);

        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

        ItemStack clicked = event.getCurrentItem();
        String displayName = clicked.getItemMeta() != null && clicked.getItemMeta().hasDisplayName() 
            ? clicked.getItemMeta().getDisplayName() : "";

        player.closeInventory();

        if (displayName.contains("Close")) {
            return;
        }
        
        if (displayName.contains("My Claims")) {
            new ClaimsListGUI(plugin, player).open();
        } else if (displayName.contains("Manage Claim")) {
            Region region = plugin.getRegionManager().getRegionAt(player.getLocation());
            if (region != null) {
                new ClaimManageGUI(plugin, player, region).open();
            }
        } else if (displayName.contains("Go to City")) {
            player.performCommand("city");
        } else if (displayName.contains("Leave City")) {
            player.performCommand("leavecity");
        } else if (displayName.contains("Help")) {
            new HelpGUI(plugin).open(player);
        } else if (displayName.contains("Admin Tools")) {
            if (player.hasPermission("skittlecities.admin")) {
                new AdminClaimsGUI(plugin, player, 0).open();
            }
        }
    }

    private ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(MessageUtil.colorize(name));
            
            if (lore.length > 0) {
                List<String> loreList = new ArrayList<>();
                for (String line : lore) {
                    loreList.add(MessageUtil.colorize(line));
                }
                meta.setLore(loreList);
            }
            
            item.setItemMeta(meta);
        }
        
        return item;
    }
}
