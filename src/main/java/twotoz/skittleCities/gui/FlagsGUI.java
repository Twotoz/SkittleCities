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
        
        // Build title based on claim type
        String title;
        if (isWorldFlags) {
            title = "World Flags";
        } else if (region.isSubclaim()) {
            // Show as "Subclaim Flags: [name] (Parent)"
            String name = region.getDisplayName() != null ? region.getDisplayName() : region.getName();
            Region parent = plugin.getRegionManager().getParentClaim(region);
            String parentName = parent != null && parent.getDisplayName() != null ? 
                parent.getDisplayName() : (parent != null ? parent.getName() : "?");
            title = "Subclaim: " + name.substring(0, Math.min(name.length(), 15)) + 
                    " (" + parentName.substring(0, Math.min(parentName.length(), 10)) + ")";
        } else {
            // Normal claim
            String name = region.getDisplayName() != null ? region.getDisplayName() : region.getName();
            title = "Flags: " + name.substring(0, Math.min(name.length(), 25));
        }
        
        this.inventory = plugin.getServer().createInventory(null, 54, title);
        
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        setupInventory();
    }

    private void setupInventory() {
        // CRITICAL FIX: Show ALL flags from config, not just region's flags
        // This ensures new flags appear even in old claims
        Map<String, Boolean> flags;
        
        if (isWorldFlags) {
            // World flags - just use what's in config
            flags = plugin.getFlagManager().getWorldFlags();
        } else {
            // Claim flags - merge region flags with defaults
            flags = new java.util.LinkedHashMap<>();
            
            // First, get ALL flags from config (these are the "available" flags)
            org.bukkit.configuration.ConfigurationSection defaultFlags = 
                plugin.getConfig().getConfigurationSection("default-claim-flags");
            
            if (defaultFlags != null) {
                for (String flagName : defaultFlags.getKeys(false)) {
                    // Check if region has this flag, otherwise use default
                    if (region.getFlags().containsKey(flagName)) {
                        flags.put(flagName, region.getFlags().get(flagName));
                    } else {
                        // Region doesn't have this flag yet - use default from config
                        flags.put(flagName, defaultFlags.getBoolean(flagName));
                    }
                }
            }
        }

        int slot = 0;
        for (Map.Entry<String, Boolean> entry : flags.entrySet()) {
            if (slot >= 45) break; // Max 45 flags

            String flagName = entry.getKey();
            boolean flagValue = entry.getValue();

            // TRUE = ALLOWED (green), FALSE = BLOCKED (red)
            ItemStack item = new ItemStack(flagValue ? Material.LIME_WOOL : Material.RED_WOOL);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(MessageUtil.colorize("&6" + formatFlagName(flagName)));
            
            // Clear explanation of what TRUE/FALSE means
            String status = flagValue ? "&a&lALLOWED" : "&c&lBLOCKED";
            String explanation = flagValue ? 
                "&7Untrusted players &aCAN &7" + getActionDescription(flagName) :
                "&7Untrusted players &cCANNOT &7" + getActionDescription(flagName);
            
            meta.setLore(Arrays.asList(
                MessageUtil.colorize(status),
                MessageUtil.colorize(explanation),
                MessageUtil.colorize("&8(Owner/Trusted always allowed)"),
                MessageUtil.colorize("&e&lClick to toggle")
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
    
    private String getActionDescription(String flagName) {
        return switch (flagName) {
            case "block-break" -> "break blocks";
            case "block-place" -> "place blocks";
            case "pvp" -> "fight other players";
            case "chest-access" -> "open chests";
            case "use-doors" -> "use doors";
            case "use-trapdoors" -> "use trapdoors";
            case "use-buttons" -> "use buttons";
            case "use-levers" -> "use levers";
            case "mob-spawning" -> "spawn (mobs)";
            case "trampling" -> "trample farmland";
            case "plant-growth" -> "grow (plants/crops)";
            case "entity-damage" -> "damage entities";
            case "explosion" -> "cause explosions";
            case "fire-spread" -> "spread fire";
            default -> "do this";
        };
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

        boolean oldValue, newValue;
        
        if (isWorldFlags) {
            oldValue = plugin.getFlagManager().getWorldFlag(flagName);
            newValue = !oldValue;
            plugin.getFlagManager().setWorldFlag(flagName, newValue);
        } else {
            oldValue = plugin.getFlagManager().getClaimFlag(region, flagName);
            newValue = !oldValue;
            plugin.getFlagManager().setClaimFlag(region, flagName, newValue);
        }

        // Send clear message about new state
        String status = newValue ? "&a&lALLOWED" : "&c&lBLOCKED";
        String action = getActionDescription(flagName);
        
        player.sendMessage(MessageUtil.colorize(
            plugin.getConfig().getString("messages.prefix") + 
            "&6" + formatFlagName(flagName) + " &7→ " + status
        ));
        
        if (newValue) {
            player.sendMessage(MessageUtil.colorize("&7Untrusted players can now " + action));
        } else {
            player.sendMessage(MessageUtil.colorize("&7Untrusted players can no longer " + action));
        }
        
        player.sendMessage(MessageUtil.colorize("&8(Owner/Trusted always allowed)"));

        setupInventory();
    }

    private String getFlagNameFromDisplay(String display) {
        String cleaned = display.replaceAll("§.", "").toLowerCase();
        return cleaned.replace(" ", "-");
    }
}
