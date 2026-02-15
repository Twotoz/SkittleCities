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
import twotoz.skittleCities.data.Selection;
import twotoz.skittleCities.utils.MessageUtil;

import java.util.Arrays;

public class RegionCreateGUI implements Listener {
    private final SkittleCities plugin;
    private final Player player;
    private final Inventory inventory;
    
    private Region.RegionType type = Region.RegionType.PRIVATE;
    private double price = 1000;
    private int leaseDays = 7;
    private boolean pvp = false;

    public RegionCreateGUI(SkittleCities plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = plugin.getServer().createInventory(null, 27, "Create Region");
        
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        setupInventory();
    }

    private void setupInventory() {
        // Type selector
        ItemStack typeItem = new ItemStack(Material.PAPER);
        ItemMeta typeMeta = typeItem.getItemMeta();
        typeMeta.setDisplayName(MessageUtil.colorize("&6Region Type: &e" + type.name()));
        typeMeta.setLore(Arrays.asList(
            MessageUtil.colorize("&7Click to cycle through:"),
            MessageUtil.colorize("&7- FOR_HIRE (lease system)"),
            MessageUtil.colorize("&7- FOR_SALE (permanent)"),
            MessageUtil.colorize("&7- PRIVATE (admin only)")
        ));
        typeItem.setItemMeta(typeMeta);
        inventory.setItem(10, typeItem);

        // Price
        updatePriceItem();

        // Lease days
        updateLeaseDaysItem();

        // PVP
        updatePvpItem();

        // Confirm button
        ItemStack confirm = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta confirmMeta = confirm.getItemMeta();
        confirmMeta.setDisplayName(MessageUtil.colorize("&a&lCREATE REGION"));
        confirm.setItemMeta(confirmMeta);
        inventory.setItem(22, confirm);

        // Cancel button
        ItemStack cancel = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta cancelMeta = cancel.getItemMeta();
        cancelMeta.setDisplayName(MessageUtil.colorize("&c&lCANCEL"));
        cancel.setItemMeta(cancelMeta);
        inventory.setItem(26, cancel);
    }

    private void updatePriceItem() {
        ItemStack priceItem = new ItemStack(Material.GOLD_INGOT);
        ItemMeta priceMeta = priceItem.getItemMeta();
        priceMeta.setDisplayName(MessageUtil.colorize("&6Price: &e$" + price));
        priceMeta.setLore(Arrays.asList(
            MessageUtil.colorize("&7Left click: +100"),
            MessageUtil.colorize("&7Right click: -100"),
            MessageUtil.colorize("&7Shift + Left: +1000"),
            MessageUtil.colorize("&7Shift + Right: -1000")
        ));
        priceItem.setItemMeta(priceMeta);
        inventory.setItem(12, priceItem);
    }

    private void updateLeaseDaysItem() {
        ItemStack daysItem = new ItemStack(Material.CLOCK);
        ItemMeta daysMeta = daysItem.getItemMeta();
        daysMeta.setDisplayName(MessageUtil.colorize("&6Lease Days: &e" + leaseDays));
        daysMeta.setLore(Arrays.asList(
            MessageUtil.colorize("&7Left click: +1"),
            MessageUtil.colorize("&7Right click: -1"),
            MessageUtil.colorize("&7Shift + Left: +7"),
            MessageUtil.colorize("&7Shift + Right: -7")
        ));
        daysItem.setItemMeta(daysMeta);
        inventory.setItem(14, daysItem);
    }

    private void updatePvpItem() {
        ItemStack pvpItem = new ItemStack(pvp ? Material.DIAMOND_SWORD : Material.SHIELD);
        ItemMeta pvpMeta = pvpItem.getItemMeta();
        pvpMeta.setDisplayName(MessageUtil.colorize("&6PVP: " + (pvp ? "&aEnabled" : "&cDisabled")));
        pvpMeta.setLore(Arrays.asList(MessageUtil.colorize("&7Click to toggle")));
        pvpItem.setItemMeta(pvpMeta);
        inventory.setItem(16, pvpItem);
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
        boolean isShift = event.isShiftClick();
        boolean isLeft = event.isLeftClick();

        switch (slot) {
            case 10: // Type
                int nextOrdinal = (type.ordinal() + 1) % Region.RegionType.values().length;
                type = Region.RegionType.values()[nextOrdinal];
                setupInventory();
                break;

            case 12: // Price
                if (isShift && isLeft) price += 1000;
                else if (isShift) price = Math.max(0, price - 1000);
                else if (isLeft) price += 100;
                else price = Math.max(0, price - 100);
                updatePriceItem();
                break;

            case 14: // Lease days
                if (isShift && isLeft) leaseDays += 7;
                else if (isShift) leaseDays = Math.max(1, leaseDays - 7);
                else if (isLeft) leaseDays += 1;
                else leaseDays = Math.max(1, leaseDays - 1);
                updateLeaseDaysItem();
                break;

            case 16: // PVP
                pvp = !pvp;
                updatePvpItem();
                break;

            case 22: // Confirm
                createRegion();
                player.closeInventory();
                break;

            case 26: // Cancel
                player.closeInventory();
                break;
        }
    }

    private void createRegion() {
        Selection selection = plugin.getSelectionManager().getSelection(player);
        
        String regionName = "region_" + System.currentTimeMillis();
        Region region = new Region(0, regionName, player.getWorld(), 
            selection.getPos1(), selection.getPos2());

        // Check for overlaps
        if (plugin.getRegionManager().hasOverlap(region)) {
            MessageUtil.send(player, plugin.getConfig(), "region-overlaps");
            return;
        }

        region.setType(type);
        region.setPrice(price);
        region.setLeaseDays(leaseDays);
        region.setAutoExtend(false); // Players can enable this with /cautoextend

        // Apply default flags
        plugin.getFlagManager().applyDefaultFlags(region);
        region.getFlags().put("pvp", pvp);

        // Set sign location if needed
        if (type != Region.RegionType.PRIVATE) {
            region.setSignLocation(region.getCenter());
        }

        plugin.getRegionManager().createRegion(region);
        plugin.getSelectionManager().clearSelection(player);

        MessageUtil.send(player, plugin.getConfig(), "region-created");
        
        // Helper messages
        if (type == Region.RegionType.FOR_HIRE || type == Region.RegionType.FOR_SALE) {
            player.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
                "&7A sign was created - players can right-click it to purchase"));
        }
        if (type == Region.RegionType.SAFEZONE) {
            player.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
                "&7Safezone created - use &e/cflags &7to configure protection"));
        }
        player.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
            "&7Use &e/cadmin &7to manage all claims"));
    }
}
