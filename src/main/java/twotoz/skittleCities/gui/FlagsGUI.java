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

import java.util.Arrays;
import java.util.Map;

public class FlagsGUI implements Listener {
    private final SkittleCities plugin;
    private final Player player;
    private final Region region; // null for world flags
    private final Inventory inventory;
    private final boolean isWorldFlags;

    public FlagsGUI(SkittleCities plugin, Player player, Region region) {
        this.plugin = plugin;
        this.player = player;
        this.region = region;
        this.isWorldFlags = (region == null);
        this.inventory = plugin.getServer().createInventory(null, 54, 
            isWorldFlags ? "World Flags" : "Claim Flags");
        
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        setupInventory();
    }

    private void setupInventory() {
        Map<String, Boolean> flags = isWorldFlags ? 
            plugin.getFlagManager().getWorldFlags() : 
            region.getFlags();

        int slot = 0;
        for (Map.Entry<String, Boolean> entry : flags.entrySet()) {
            if (slot >= 45) break; // Max 45 flags

            String flagName = entry.getKey();
            boolean flagValue = entry.getValue();

            ItemStack item = new ItemStack(flagValue ? Material.GREEN_WOOL : Material.RED_WOOL);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(MessageUtil.colorize("&6" + formatFlagName(flagName)));
            meta.setLore(Arrays.asList(
                MessageUtil.colorize("&7Status: " + (flagValue ? "&aEnabled" : "&cDisabled")),
                MessageUtil.colorize("&7Click to toggle")
            ));
            item.setItemMeta(meta);

            inventory.setItem(slot, item);
            slot++;
        }

        // Close button
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        closeMeta.setDisplayName(MessageUtil.colorize("&c&lClose"));
        close.setItemMeta(closeMeta);
        inventory.setItem(49, close);
    }

    private String formatFlagName(String flag) {
        return Arrays.stream(flag.split("-"))
            .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1))
            .reduce((a, b) -> a + " " + b)
            .orElse(flag);
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
        
        if (slot == 49) {
            player.closeInventory();
            return;
        }

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        ItemMeta meta = clicked.getItemMeta();
        if (meta == null) return;

        String displayName = meta.getDisplayName();
        String flagName = getFlagNameFromDisplay(displayName);

        if (isWorldFlags) {
            boolean currentValue = plugin.getFlagManager().getWorldFlag(flagName);
            plugin.getFlagManager().setWorldFlag(flagName, !currentValue);
        } else {
            boolean currentValue = plugin.getFlagManager().getClaimFlag(region, flagName);
            plugin.getFlagManager().setClaimFlag(region, flagName, !currentValue);
        }

        MessageUtil.send(player, plugin.getConfig(), "flag-updated",
            new String[]{"%flag%", "%value%"},
            new String[]{flagName, String.valueOf(!getFlagCurrentValue(flagName))});

        setupInventory();
    }

    private String getFlagNameFromDisplay(String display) {
        String cleaned = display.replaceAll("§.", "").toLowerCase();
        return cleaned.replace(" ", "-");
    }

    private boolean getFlagCurrentValue(String flagName) {
        if (isWorldFlags) {
            return plugin.getFlagManager().getWorldFlag(flagName);
        } else {
            return plugin.getFlagManager().getClaimFlag(region, flagName);
        }
    }
}
