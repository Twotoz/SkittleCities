package twotoz.skittleCities.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import twotoz.skittleCities.SkittleCities;
import twotoz.skittleCities.data.Region;
import twotoz.skittleCities.utils.MessageUtil;

public class SignListener implements Listener {
    private final SkittleCities plugin;

    public SignListener(SkittleCities plugin) {
        this.plugin = plugin;
    }

    /**
     * Block player-created sell/buy signs (admin only via /csellsign and /cbuysign)
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();
        
        // WORLD CHECK - Only work in city world
        String cityWorld = plugin.getConfig().getString("world-name", "city");
        if (!event.getBlock().getWorld().getName().equals(cityWorld)) {
            return; // Ignore signs in other worlds
        }
        
        String line0 = event.getLine(0);
        
        if (line0 == null) return;
        
        // Check if player is trying to create a [SELL] or [BUY] sign manually
        String stripped = org.bukkit.ChatColor.stripColor(line0).toLowerCase().trim();
        
        if (stripped.equals("sell") || stripped.equals("buy") || 
            stripped.equals("[sell]") || stripped.equals("[buy]")) {
            
            // BLOCK - Only commands can create these signs
            event.setCancelled(true);
            player.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
                "&cUse commands to create shop signs!"));
            player.sendMessage(MessageUtil.colorize("&7[SELL] signs (players buy): &e/csellsign <material> <price>"));
            player.sendMessage(MessageUtil.colorize("&7[BUY] signs (players sell): &e/cbuysign <material> <price>"));
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        
        Block block = event.getClickedBlock();
        if (block == null) return;
        if (!(block.getState() instanceof Sign)) return;

        // WORLD CHECK - Only work in city world
        String cityWorld = plugin.getConfig().getString("world-name", "city");
        if (!block.getWorld().getName().equals(cityWorld)) {
            return; // Ignore signs in other worlds
        }

        Player player = event.getPlayer();
        Sign sign = (Sign) block.getState();

        // Check if it's a SELL sign first
        if (plugin.getSellSignManager().isSellSign(sign)) {
            event.setCancelled(true); // Prevent sign editor
            plugin.getSellSignManager().handleSell(player, sign);
            return;
        }

        // Find region with this sign location (FOR SALE/FOR HIRE claims)
        Region region = null;
        for (Region r : plugin.getRegionManager().getAllRegions()) {
            if (r.getSignLocation() != null && 
                r.getSignLocation().getBlockX() == block.getX() &&
                r.getSignLocation().getBlockY() == block.getY() &&
                r.getSignLocation().getBlockZ() == block.getZ()) {
                region = r;
                break;
            }
        }

        if (region == null) return;
        if (region.getOwner() != null) return; // Already owned

        // IMPORTANT: Cancel event to prevent sign editor from opening
        event.setCancelled(true);

        double price = region.getPrice();

        // Check balance
        if (!plugin.getEconomyManager().hasBalance(player.getUniqueId(), price)) {
            MessageUtil.send(player, plugin.getConfig(), "insufficient-funds",
                new String[]{"%amount%"},
                new String[]{String.valueOf(price)});
            return;
        }

        // Withdraw money
        plugin.getEconomyManager().withdraw(player.getUniqueId(), price);

        // Set owner
        region.setOwner(player.getUniqueId());

        // Remove the sign block since claim is now owned
        block.setType(org.bukkit.Material.AIR);

        if (region.getType() == Region.RegionType.FOR_HIRE) {
            // Calculate lease expiry
            long expiryTime = System.currentTimeMillis() + 
                (region.getLeaseDays() * 24L * 60L * 60L * 1000L);
            region.setLeaseExpiry(expiryTime);

            MessageUtil.send(player, plugin.getConfig(), "lease-success",
                new String[]{"%amount%", "%days%"},
                new String[]{String.valueOf(price), String.valueOf(region.getLeaseDays())});
            
            // Helper message
            player.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
                "&7Use &e/cclaims &7to manage your claims • &e/ctrust <player> &7to add builders"));
        } else {
            MessageUtil.send(player, plugin.getConfig(), "purchase-success",
                new String[]{"%amount%"},
                new String[]{String.valueOf(price)});
            
            // Helper message
            player.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
                "&7Use &e/cclaims &7to manage your claims • &e/ctrust <player> &7to add builders"));
        }

        // Update region
        plugin.getRegionManager().updateRegion(region);

        // Remove sign
        block.setType(org.bukkit.Material.AIR);
    }
}
