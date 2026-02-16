package twotoz.skittleCities.gui;

import org.bukkit.Bukkit;
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

public class AdminClaimsGUI implements Listener {
    private final SkittleCities plugin;
    private final Player player;
    private final Inventory inventory;
    private final List<Region> allClaims;
    private int page;
    private static final int ITEMS_PER_PAGE = 45;

    public AdminClaimsGUI(SkittleCities plugin, Player player) {
        this(plugin, player, 0);
    }

    public AdminClaimsGUI(SkittleCities plugin, Player player, int page) {
        this.plugin = plugin;
        this.player = player;
        this.page = page;
        this.allClaims = plugin.getRegionManager().getAllRegions();
        this.inventory = plugin.getServer().createInventory(null, 54, "All Claims (Page " + (page + 1) + ")");
        
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        setupInventory();
    }

    private void setupInventory() {
        int startIndex = page * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, allClaims.size());

        int slot = 0;
        for (int i = startIndex; i < endIndex; i++) {
            Region region = allClaims.get(i);
            
            Material material;
            if (region.isSubclaim()) {
                // SUBCLAIMS = CYAN WOOL (blue-ish)
                material = Material.CYAN_WOOL;
            } else {
                // Normal claims based on type
                switch (region.getType()) {
                    case FOR_HIRE -> material = Material.GOLD_BLOCK;
                    case FOR_SALE -> material = Material.DIAMOND_BLOCK;
                    case SAFEZONE -> material = Material.EMERALD_BLOCK;
                    default -> material = Material.STONE;
                }
            }
            
            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            
            // Display name
            if (region.isSubclaim()) {
                Region parent = plugin.getRegionManager().getParentClaim(region);
                String parentName = parent != null && parent.getDisplayName() != null ? 
                    parent.getDisplayName() : (parent != null ? parent.getName() : "Unknown");
                meta.setDisplayName(MessageUtil.colorize("&b[SUBCLAIM] &e" + parentName));
            } else {
                String displayName = region.getDisplayName() != null ? region.getDisplayName() : region.getName();
                meta.setDisplayName(MessageUtil.colorize("&6" + displayName));
            }
            
            List<String> lore = new ArrayList<>();
            lore.add(MessageUtil.colorize("&7ID: &e" + region.getId()));
            
            if (region.isSubclaim()) {
                lore.add(MessageUtil.colorize("&7Type: &bSUBCLAIM"));
                Region parent = plugin.getRegionManager().getParentClaim(region);
                if (parent != null) {
                    String parentName = parent.getDisplayName() != null ? parent.getDisplayName() : parent.getName();
                    lore.add(MessageUtil.colorize("&7Parent: &e" + parentName + " &7(ID: " + parent.getId() + ")"));
                }
                lore.add(MessageUtil.colorize("&7Technical: &f" + region.getName()));
            } else {
                lore.add(MessageUtil.colorize("&7Type: &e" + region.getType().name()));
            }
            
            String ownerName = "None";
            if (region.getOwner() != null) {
                ownerName = plugin.getServer().getOfflinePlayer(region.getOwner()).getName();
                if (ownerName == null) ownerName = "Unknown";
            }
            lore.add(MessageUtil.colorize("&7Owner: &e" + ownerName));
            
            lore.add(MessageUtil.colorize("&7Location: &e" + 
                (int)region.getCenter().getX() + ", " + 
                (int)region.getCenter().getY() + ", " + 
                (int)region.getCenter().getZ()));
            
            if (!region.isSubclaim() && (region.getType() == Region.RegionType.FOR_HIRE || region.getType() == Region.RegionType.FOR_SALE)) {
                lore.add(MessageUtil.colorize("&7Price: &e$" + region.getPrice()));
            }
            
            lore.add("");
            lore.add(MessageUtil.colorize("&eClick to edit/manage"));
            
            meta.setLore(lore);
            item.setItemMeta(meta);
            
            inventory.setItem(slot, item);
            slot++;
        }

        // Navigation buttons
        if (page > 0) {
            ItemStack prev = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prev.getItemMeta();
            prevMeta.setDisplayName(MessageUtil.colorize("&e&lPrevious Page"));
            prev.setItemMeta(prevMeta);
            inventory.setItem(48, prev);
        }

        if (endIndex < allClaims.size()) {
            ItemStack next = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = next.getItemMeta();
            nextMeta.setDisplayName(MessageUtil.colorize("&e&lNext Page"));
            next.setItemMeta(nextMeta);
            inventory.setItem(50, next);
        }

        // Close button
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        closeMeta.setDisplayName(MessageUtil.colorize("&c&lClose"));
        close.setItemMeta(closeMeta);
        inventory.setItem(49, close);
    }

    public void open() {
        player.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        if (!(event.getWhoClicked() instanceof Player)) return;

        event.setCancelled(true);

        int slot = event.getRawSlot();

        if (slot == 48 && page > 0) {
            // Previous page
            player.closeInventory();
            AdminClaimsGUI prevGUI = new AdminClaimsGUI(plugin, player, page - 1);
            prevGUI.open();
        } else if (slot == 50 && (page + 1) * ITEMS_PER_PAGE < allClaims.size()) {
            // Next page
            player.closeInventory();
            AdminClaimsGUI nextGUI = new AdminClaimsGUI(plugin, player, page + 1);
            nextGUI.open();
        } else if (slot == 49) {
            // Close
            player.closeInventory();
        } else if (slot >= 0 && slot < ITEMS_PER_PAGE) {
            // Clicked on a claim
            int claimIndex = (page * ITEMS_PER_PAGE) + slot;
            if (claimIndex < allClaims.size()) {
                Region region = allClaims.get(claimIndex);
                player.closeInventory();
                
                AdminEditClaimGUI editGUI = new AdminEditClaimGUI(plugin, player, region);
                editGUI.open();
            }
        }
    }
}
