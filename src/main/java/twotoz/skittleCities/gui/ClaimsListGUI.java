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
import java.util.concurrent.TimeUnit;

public class ClaimsListGUI implements Listener {
    private final SkittleCities plugin;
    private final Player player;
    private final Inventory inventory;
    private final List<Region> playerClaims;

    public ClaimsListGUI(SkittleCities plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.playerClaims = new ArrayList<>();
        
        // Find all claims owned by this player
        for (Region region : plugin.getRegionManager().getAllRegions()) {
            if (region.getOwner() != null && region.getOwner().equals(player.getUniqueId())) {
                playerClaims.add(region);
            }
        }
        
        this.inventory = Bukkit.createInventory(null, 54, "My Claims (" + playerClaims.size() + ")");
        
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        
        if (playerClaims.isEmpty()) {
            // Don't setup inventory if no claims
            return;
        }
        
        setupInventory();
    }

    private void setupInventory() {
        int slot = 0;
        for (Region region : playerClaims) {
            if (slot >= 45) break; // Max 45 claims visible

            Material material = region.getType() == Region.RegionType.FOR_HIRE ? 
                Material.GOLD_BLOCK : Material.DIAMOND_BLOCK;
            
            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(MessageUtil.colorize("&6" + region.getName()));
            
            List<String> lore = new ArrayList<>();
            lore.add(MessageUtil.colorize("&7Type: &e" + region.getType().name()));
            lore.add(MessageUtil.colorize("&7Location: &e" + 
                (int)region.getCenter().getX() + ", " + 
                (int)region.getCenter().getY() + ", " + 
                (int)region.getCenter().getZ()));
            
            if (region.getType() == Region.RegionType.FOR_HIRE) {
                long timeLeft = region.getLeaseExpiry() - System.currentTimeMillis();
                if (timeLeft > 0) {
                    long days = TimeUnit.MILLISECONDS.toDays(timeLeft);
                    long hours = TimeUnit.MILLISECONDS.toHours(timeLeft) % 24;
                    lore.add(MessageUtil.colorize("&7Time left: &e" + days + "d " + hours + "h"));
                } else {
                    lore.add(MessageUtil.colorize("&cExpired!"));
                }
                lore.add(MessageUtil.colorize("&7Auto-extend: " + 
                    (region.isAutoExtend() ? "&aEnabled" : "&cDisabled")));
            }
            
            lore.add(MessageUtil.colorize("&7Trusted: &e" + region.getTrustedPlayers().size() + " player(s)"));
            lore.add("");
            lore.add(MessageUtil.colorize("&eClick to manage"));
            
            meta.setLore(lore);
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

    public void open() {
        if (playerClaims.isEmpty()) {
            player.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
                "&7You don't own any claims yet!"));
            player.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
                "&7Look for &e[FOR HIRE] &7or &e[FOR SALE] &7signs to purchase claims"));
            return;
        }
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

        if (slot >= 0 && slot < playerClaims.size()) {
            Region region = playerClaims.get(slot);
            player.closeInventory();
            
            ClaimManageGUI manageGUI = new ClaimManageGUI(plugin, player, region);
            manageGUI.open();
        }
    }
}
