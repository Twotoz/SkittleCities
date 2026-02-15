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
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ClaimManageGUI implements Listener {
    private final SkittleCities plugin;
    private final Player player;
    private final Region region;
    private final Inventory inventory;

    public ClaimManageGUI(SkittleCities plugin, Player player, Region region) {
        this.plugin = plugin;
        this.player = player;
        this.region = region;
        this.inventory = plugin.getServer().createInventory(null, 27, "Manage: " + region.getName());
        
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        setupInventory();
    }

    private void setupInventory() {
        // Teleport button
        ItemStack teleport = new ItemStack(Material.ENDER_PEARL);
        ItemMeta teleportMeta = teleport.getItemMeta();
        teleportMeta.setDisplayName(MessageUtil.colorize("&a&lTeleport"));
        teleportMeta.setLore(List.of(MessageUtil.colorize("&7Click to teleport to this claim")));
        teleport.setItemMeta(teleportMeta);
        inventory.setItem(10, teleport);

        // Auto-extend toggle (only for leased claims)
        if (region.getType() == Region.RegionType.FOR_HIRE) {
            ItemStack autoExtend = new ItemStack(
                region.isAutoExtend() ? Material.GREEN_WOOL : Material.RED_WOOL);
            ItemMeta autoMeta = autoExtend.getItemMeta();
            autoMeta.setDisplayName(MessageUtil.colorize("&6Auto-Extend: " + 
                (region.isAutoExtend() ? "&aEnabled" : "&cDisabled")));
            
            List<String> autoLore = new ArrayList<>();
            autoLore.add(MessageUtil.colorize("&7Click to toggle"));
            autoLore.add("");
            
            if (region.isAutoExtend()) {
                autoLore.add(MessageUtil.colorize("&7Your lease will automatically"));
                autoLore.add(MessageUtil.colorize("&7renew if you have funds"));
            } else {
                autoLore.add(MessageUtil.colorize("&7Your lease will expire"));
                autoLore.add(MessageUtil.colorize("&7when time runs out"));
            }
            
            autoMeta.setLore(autoLore);
            autoExtend.setItemMeta(autoMeta);
            inventory.setItem(12, autoExtend);
        }

        // Trusted players
        ItemStack trusted = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta trustedMeta = trusted.getItemMeta();
        trustedMeta.setDisplayName(MessageUtil.colorize("&6Trusted Players"));
        
        List<String> trustedLore = new ArrayList<>();
        trustedLore.add(MessageUtil.colorize("&7Currently trusted: &e" + 
            region.getTrustedPlayers().size()));
        trustedLore.add("");
        
        for (UUID uuid : region.getTrustedPlayers()) {
            String name = plugin.getServer().getOfflinePlayer(uuid).getName();
            if (name != null) {
                trustedLore.add(MessageUtil.colorize("&7• &e" + name));
            }
        }
        
        if (region.getTrustedPlayers().isEmpty()) {
            trustedLore.add(MessageUtil.colorize("&7No trusted players yet"));
            trustedLore.add(MessageUtil.colorize("&7Use /ctrust <player>"));
        }
        
        trustedMeta.setLore(trustedLore);
        trusted.setItemMeta(trustedMeta);
        inventory.setItem(14, trusted);

        // Info item
        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = info.getItemMeta();
        infoMeta.setDisplayName(MessageUtil.colorize("&6Claim Information"));
        
        List<String> infoLore = new ArrayList<>();
        infoLore.add(MessageUtil.colorize("&7Type: &e" + region.getType().name()));
        infoLore.add(MessageUtil.colorize("&7Location: &e" + 
            (int)region.getCenter().getX() + ", " + 
            (int)region.getCenter().getY() + ", " + 
            (int)region.getCenter().getZ()));
        
        if (region.getType() == Region.RegionType.FOR_HIRE) {
            infoLore.add(MessageUtil.colorize("&7Price: &e$" + region.getPrice()));
            infoLore.add(MessageUtil.colorize("&7Lease days: &e" + region.getLeaseDays()));
            
            long timeLeft = region.getLeaseExpiry() - System.currentTimeMillis();
            if (timeLeft > 0) {
                long days = TimeUnit.MILLISECONDS.toDays(timeLeft);
                long hours = TimeUnit.MILLISECONDS.toHours(timeLeft) % 24;
                long minutes = TimeUnit.MILLISECONDS.toMinutes(timeLeft) % 60;
                infoLore.add(MessageUtil.colorize("&7Time left: &e" + days + "d " + hours + "h " + minutes + "m"));
            } else {
                infoLore.add(MessageUtil.colorize("&c&lExpired!"));
            }
        }
        
        infoMeta.setLore(infoLore);
        info.setItemMeta(infoMeta);
        inventory.setItem(16, info);

        // Cancel lease button (only for leased claims)
        if (region.getType() == Region.RegionType.FOR_HIRE) {
            ItemStack cancel = new ItemStack(Material.TNT);
            ItemMeta cancelMeta = cancel.getItemMeta();
            cancelMeta.setDisplayName(MessageUtil.colorize("&c&lCancel Lease"));
            cancelMeta.setLore(List.of(
                MessageUtil.colorize("&7Cancel your lease and"),
                MessageUtil.colorize("&7give up this claim"),
                MessageUtil.colorize("&c&lWARNING: No refund!")
            ));
            cancel.setItemMeta(cancelMeta);
            inventory.setItem(22, cancel);
        }

        // Back button
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName(MessageUtil.colorize("&e&lBack to Claims List"));
        back.setItemMeta(backMeta);
        inventory.setItem(18, back);

        // Close button
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        closeMeta.setDisplayName(MessageUtil.colorize("&c&lClose"));
        close.setItemMeta(closeMeta);
        inventory.setItem(26, close);
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

        switch (slot) {
            case 10: // Teleport
                player.closeInventory();
                player.teleport(region.getCenter());
                player.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
                    "&aTeleported to your claim!"));
                break;

            case 12: // Auto-extend toggle
                if (region.getType() == Region.RegionType.FOR_HIRE) {
                    region.setAutoExtend(!region.isAutoExtend());
                    plugin.getRegionManager().updateRegion(region);
                    
                    player.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
                        (region.isAutoExtend() ? 
                            "&aAuto-extend enabled!" : 
                            "&cAuto-extend disabled!")));
                    
                    setupInventory();
                }
                break;

            case 18: // Back
                player.closeInventory();
                ClaimsListGUI listGUI = new ClaimsListGUI(plugin, player);
                listGUI.open();
                break;

            case 22: // Cancel lease
                if (region.getType() == Region.RegionType.FOR_HIRE) {
                    player.closeInventory();
                    
                    // Reset the region
                    region.setOwner(null);
                    region.setLeaseExpiry(0);
                    region.setAutoExtend(false);
                    region.getTrustedPlayers().clear();
                    
                    // Update in database
                    plugin.getRegionManager().updateRegion(region);
                    
                    // Recreate sign
                    plugin.getRegionManager().recreateSign(region);
                    
                    player.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
                        "&cYou cancelled your lease. The claim is now available again."));
                }
                break;

            case 26: // Close
                player.closeInventory();
                break;
        }
    }
}
