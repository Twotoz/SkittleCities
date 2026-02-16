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
import twotoz.skittleCities.utils.MessageUtil;

import java.util.ArrayList;
import java.util.List;

public class HelpGUI implements Listener {
    private final SkittleCities plugin;

    public HelpGUI(SkittleCities plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Inventory inv = plugin.getServer().createInventory(null, 54, MessageUtil.colorize("&6&lHelp &8| &7Commands"));
        
        // Player Commands
        inv.setItem(10, createItem(Material.GRASS_BLOCK, "&aClaim Commands",
            "&e/cclaims &7- View your claims",
            "&e/ctrust <player> &7- Trust a player",
            "&e/cuntrust <player> &7- Untrust a player",
            "&e/cautoextend &7- Toggle auto-renewal",
            "",
            "&7Purchase claims by right-clicking",
            "&7[FOR SALE] or [FOR HIRE] signs!"));
        
        inv.setItem(12, createItem(Material.GOLD_INGOT, "&eEconomy Commands",
            "&e/cbal &7- Check your balance",
            "",
            "&7Buy claims with your balance",
            "&7Leases auto-renew if enabled"));
        
        inv.setItem(14, createItem(Material.EMERALD, "&aNavigation",
            "&e/city &7- Teleport to city",
            "&e/leavecity &7- Leave city",
            "&e/cmenu &7- Open this menu",
            "",
            "&7Works from anywhere!"));
        
        inv.setItem(16, createItem(Material.PLAYER_HEAD, "&aTrusting Players",
            "&7Trust allows players to:",
            "&a✓ &7Break/place blocks",
            "&a✓ &7Open chests",
            "&a✓ &7Use doors/buttons",
            "",
            "&7Stand in your claim and use:",
            "&e/ctrust <player>"));
        
        // Admin Commands (if permission)
        if (player.hasPermission("skittlecities.admin")) {
            inv.setItem(28, createItem(Material.COMMAND_BLOCK, "&cAdmin Commands",
                "&e/ctool &7- Get selection tool",
                "&e/cregioncreate &7- Create region",
                "&e/cadmin &7- Manage all claims",
                "&e/cflags &7- Edit flags",
                "&e/cignoreclaims &7- Bypass protection",
                "&e/citycommandbypass &7- Bypass commands",
                "&e/ceconomy <give|take|set> &7- Manage economy"));
        }
        
        // Info Sections
        inv.setItem(30, createItem(Material.BOOK, "&eWhat are Claims?",
            "&7Claims are protected areas where",
            "&7only you and trusted players can build.",
            "",
            "&7Types:",
            "&a• FOR_SALE &7- One-time purchase",
            "&a• FOR_HIRE &7- Time-based lease",
            "&a• SAFEZONE &7- Protected spawn area",
            "&a• PRIVATE &7- Admin-only area"));
        
        inv.setItem(32, createItem(Material.CLOCK, "&eLeases & Auto-Renewal",
            "&7FOR_HIRE claims are rented, not owned.",
            "",
            "&7Use &e/cautoextend &7to toggle auto-renewal.",
            "&7If enabled, lease auto-renews daily",
            "&7as long as you have balance.",
            "",
            "&cIf lease expires, you lose the claim!"));
        
        inv.setItem(34, createItem(Material.REDSTONE, "&eFlags & Protection",
            "&7Flags control what's allowed in claims.",
            "",
            "&7Examples:",
            "&a• pvp &7- PvP allowed?",
            "&a• mob-spawning &7- Mobs spawn?",
            "&a• plant-growth &7- Crops grow?",
            "&a• trampling &7- Farmland protected?",
            "",
            "&7Admins use &e/cflags &7to configure"));
        
        // Back button
        inv.setItem(49, createItem(Material.ARROW, "&aBack to Menu", ""));
        
        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getView().getTitle() == null) return;
        if (!event.getView().getTitle().contains("Help")) return;

        event.setCancelled(true);

        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

        ItemStack clicked = event.getCurrentItem();
        String displayName = clicked.getItemMeta() != null && clicked.getItemMeta().hasDisplayName() 
            ? clicked.getItemMeta().getDisplayName() : "";

        if (displayName.contains("Back to Menu")) {
            player.closeInventory();
            new MainMenuGUI(plugin).open(player);
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
