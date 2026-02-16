package twotoz.skittleCities.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import twotoz.skittleCities.SkittleCities;
import twotoz.skittleCities.gui.MainMenuGUI;
import twotoz.skittleCities.utils.MessageUtil;

import java.util.Arrays;

public class MenuItemListener implements Listener {
    private final SkittleCities plugin;
    private static final String MENU_ITEM_NAME = "§6§lCity Menu";
    private static final Material MENU_ITEM_TYPE = Material.NETHER_STAR;

    public MenuItemListener(SkittleCities plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Only give menu item if in city world
        String cityWorld = plugin.getConfig().getString("world-name", "city");
        String playerWorld = player.getWorld().getName();
        
        plugin.getLogger().info("Player " + player.getName() + " joined in world: " + playerWorld + " (city world: " + cityWorld + ")");
        
        if (!playerWorld.equals(cityWorld)) {
            plugin.getLogger().info("Not in city world, skipping menu item");
            return;
        }
        
        plugin.getLogger().info("Giving menu item to " + player.getName());
        
        // Delay 1 tick to ensure player is fully loaded
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            giveMenuItem(player);
        }, 1L);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        
        // Only give menu item if respawning in city world
        String cityWorld = plugin.getConfig().getString("world-name", "city");
        if (!event.getRespawnLocation().getWorld().getName().equals(cityWorld)) {
            return;
        }
        
        // Delay 1 tick to ensure inventory is ready
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            giveMenuItem(player);
        }, 1L);
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        String cityWorld = plugin.getConfig().getString("world-name", "city");
        
        if (player.getWorld().getName().equals(cityWorld)) {
            // Entered city world - give menu item
            giveMenuItem(player);
        } else {
            // Left city world - remove menu item
            removeMenuItem(player);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        // Keep menu item on death
        event.getDrops().removeIf(this::isMenuItem);
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        // Prevent dropping menu item
        if (isMenuItem(event.getItemDrop().getItemStack())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(MessageUtil.colorize(
                plugin.getConfig().getString("messages.prefix") + 
                "&cYou cannot drop the menu item!"));
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null) return;
        if (!isMenuItem(item)) return;

        // Only work in city world
        String cityWorld = plugin.getConfig().getString("world-name", "city");
        if (!player.getWorld().getName().equals(cityWorld)) {
            return;
        }

        // Right-click or left-click - open menu
        if (event.getAction() == Action.RIGHT_CLICK_AIR || 
            event.getAction() == Action.RIGHT_CLICK_BLOCK ||
            event.getAction() == Action.LEFT_CLICK_AIR ||
            event.getAction() == Action.LEFT_CLICK_BLOCK) {
            
            event.setCancelled(true);
            
            // Open main menu
            MainMenuGUI menu = new MainMenuGUI(plugin);
            menu.open(player);
        }
    }

    /**
     * Give menu item to player if they don't have it (PUBLIC - called from commands)
     */
    public void giveMenuItemIfNeeded(Player player) {
        giveMenuItem(player);
    }

    /**
     * Give menu item to player if they don't have it
     */
    private void giveMenuItem(Player player) {
        // Check if player already has menu item
        if (hasMenuItem(player)) {
            return;
        }

        ItemStack menuItem = createMenuItem();
        
        // Add to slot 0 (first hotbar slot - bottom left)
        // Hotbar slots are 0-8 in Bukkit API
        if (player.getInventory().getItem(0) == null) {
            player.getInventory().setItem(0, menuItem);
        } else {
            // Slot 0 occupied, try to add anywhere in inventory
            player.getInventory().addItem(menuItem);
        }
    }

    /**
     * Remove menu item from player inventory
     */
    private void removeMenuItem(Player player) {
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (isMenuItem(item)) {
                player.getInventory().setItem(i, null);
            }
        }
    }

    /**
     * Check if player has menu item
     */
    private boolean hasMenuItem(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (isMenuItem(item)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Create menu item
     */
    private ItemStack createMenuItem() {
        ItemStack item = new ItemStack(MENU_ITEM_TYPE);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(MENU_ITEM_NAME);
            meta.setLore(Arrays.asList(
                MessageUtil.colorize("&7Click to open the city menu"),
                MessageUtil.colorize("&8Cannot be dropped or destroyed")
            ));
            item.setItemMeta(meta);
        }
        
        return item;
    }

    /**
     * Check if item is the menu item
     */
    private boolean isMenuItem(ItemStack item) {
        if (item == null || item.getType() != MENU_ITEM_TYPE) {
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return false;
        }
        
        return meta.getDisplayName().equals(MENU_ITEM_NAME);
    }
}
