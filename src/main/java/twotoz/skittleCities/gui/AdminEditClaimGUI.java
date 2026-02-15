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
import java.util.Arrays;
import java.util.List;

public class AdminEditClaimGUI implements Listener {
    private final SkittleCities plugin;
    private final Player player;
    private final Region region;
    private final Inventory inventory;

    public AdminEditClaimGUI(SkittleCities plugin, Player player, Region region) {
        this.plugin = plugin;
        this.player = player;
        this.region = region;
        this.inventory = Bukkit.createInventory(null, 27, "Edit: " + region.getName());
        
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        setupInventory();
    }

    private void setupInventory() {
        // Teleport
        ItemStack teleport = new ItemStack(Material.ENDER_PEARL);
        ItemMeta teleportMeta = teleport.getItemMeta();
        teleportMeta.setDisplayName(MessageUtil.colorize("&a&lTeleport"));
        teleportMeta.setLore(List.of(MessageUtil.colorize("&7Click to teleport to this claim")));
        teleport.setItemMeta(teleportMeta);
        inventory.setItem(10, teleport);

        // Recreate Sign (only if not owned)
        if (region.getOwner() == null && 
            (region.getType() == Region.RegionType.FOR_HIRE || region.getType() == Region.RegionType.FOR_SALE)) {
            ItemStack signItem = new ItemStack(Material.OAK_SIGN);
            ItemMeta signMeta = signItem.getItemMeta();
            signMeta.setDisplayName(MessageUtil.colorize("&6&lRecreate Sign"));
            signMeta.setLore(Arrays.asList(
                MessageUtil.colorize("&7Click to recreate the claim sign"),
                MessageUtil.colorize("&7Useful if sign was destroyed")
            ));
            signItem.setItemMeta(signMeta);
            inventory.setItem(11, signItem);
        }

        // Cancel Ownership (only if owned)
        if (region.getOwner() != null && 
            (region.getType() == Region.RegionType.FOR_HIRE || region.getType() == Region.RegionType.FOR_SALE)) {
            ItemStack cancel = new ItemStack(Material.BARRIER);
            ItemMeta cancelMeta = cancel.getItemMeta();
            cancelMeta.setDisplayName(MessageUtil.colorize("&c&lCancel Ownership"));
            cancelMeta.setLore(Arrays.asList(
                MessageUtil.colorize("&7Reset ownership and recreate sign"),
                MessageUtil.colorize("&cOwner will lose access!")
            ));
            cancel.setItemMeta(cancelMeta);
            inventory.setItem(11, cancel);
        }

        // Info
        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = info.getItemMeta();
        infoMeta.setDisplayName(MessageUtil.colorize("&6Claim Information"));
        
        List<String> infoLore = new ArrayList<>();
        infoLore.add(MessageUtil.colorize("&7ID: &e" + region.getId()));
        infoLore.add(MessageUtil.colorize("&7Type: &e" + region.getType().name()));
        
        String ownerName = "None";
        if (region.getOwner() != null) {
            ownerName = Bukkit.getOfflinePlayer(region.getOwner()).getName();
            if (ownerName == null) ownerName = "Unknown";
        }
        infoLore.add(MessageUtil.colorize("&7Owner: &e" + ownerName));
        infoLore.add(MessageUtil.colorize("&7Trusted: &e" + region.getTrustedPlayers().size()));
        
        if (region.getType() == Region.RegionType.FOR_HIRE || region.getType() == Region.RegionType.FOR_SALE) {
            infoLore.add(MessageUtil.colorize("&7Price: &e$" + region.getPrice()));
        }
        
        infoMeta.setLore(infoLore);
        info.setItemMeta(infoMeta);
        inventory.setItem(14, info);

        // Delete claim
        ItemStack delete = new ItemStack(Material.TNT);
        ItemMeta deleteMeta = delete.getItemMeta();
        deleteMeta.setDisplayName(MessageUtil.colorize("&c&lDELETE CLAIM"));
        deleteMeta.setLore(Arrays.asList(
            MessageUtil.colorize("&7Click to permanently delete"),
            MessageUtil.colorize("&c&lWARNING: Cannot be undone!")
        ));
        delete.setItemMeta(deleteMeta);
        inventory.setItem(16, delete);

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
                    "&aTeleported to claim!"));
                break;

            case 11: // Recreate sign OR Cancel ownership
                if (region.getOwner() == null) {
                    // Recreate sign
                    plugin.getRegionManager().recreateSign(region);
                    player.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
                        "&aSign recreated!"));
                } else {
                    // Cancel ownership
                    plugin.getRegionManager().handleLeaseExpiry(region); // This resets ownership + recreates sign
                    player.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
                        "&cOwnership cancelled and sign recreated!"));
                    player.closeInventory();
                    // Reopen GUI to show updated state
                    AdminEditClaimGUI newGUI = new AdminEditClaimGUI(plugin, player, region);
                    newGUI.open();
                }
                break;

            case 16: // Delete
                player.closeInventory();
                plugin.getRegionManager().deleteRegion(region);
                player.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
                    "&cClaim deleted!"));
                break;

            case 18: // Back
                player.closeInventory();
                AdminClaimsGUI adminGUI = new AdminClaimsGUI(plugin, player);
                adminGUI.open();
                break;

            case 26: // Close
                player.closeInventory();
                break;
        }
    }
}
