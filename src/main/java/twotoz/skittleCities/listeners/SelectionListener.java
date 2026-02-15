package twotoz.skittleCities.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import twotoz.skittleCities.SkittleCities;
import twotoz.skittleCities.data.Selection;
import twotoz.skittleCities.utils.MessageUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SelectionListener implements Listener {
    private final SkittleCities plugin;
    private final Map<UUID, Long> lastClickTime;

    public SelectionListener(SkittleCities plugin) {
        this.plugin = plugin;
        this.lastClickTime = new HashMap<>();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        // Check if player is holding the selection tool
        String toolName = plugin.getConfig().getString("selection-tool", "WOODEN_AXE");
        Material toolMaterial = Material.valueOf(toolName);

        if (item.getType() != toolMaterial) return;
        if (item.getItemMeta() == null) return;
        if (!item.getItemMeta().hasDisplayName()) return;
        if (!item.getItemMeta().getDisplayName().contains("Region Selection Tool")) return;

        if (!player.hasPermission("skittlecities.admin")) return;

        event.setCancelled(true);

        // 100ms delay to prevent double-click
        long currentTime = System.currentTimeMillis();
        Long lastClick = lastClickTime.get(player.getUniqueId());
        if (lastClick != null && currentTime - lastClick < 100) {
            return;
        }
        lastClickTime.put(player.getUniqueId(), currentTime);

        if (event.getClickedBlock() == null) return;

        Selection selection = plugin.getSelectionManager().getSelection(player);

        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            selection.setPos1(event.getClickedBlock().getLocation());
            MessageUtil.send(player, plugin.getConfig(), "selection-first-point",
                new String[]{"%x%", "%y%", "%z%"},
                new String[]{
                    String.valueOf(event.getClickedBlock().getX()),
                    String.valueOf(event.getClickedBlock().getY()),
                    String.valueOf(event.getClickedBlock().getZ())
                });
            
            // Show helper if selection is complete
            if (selection.isComplete()) {
                player.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
                    "&7Use &e/cregioncreate &7to create a region"));
            }
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            selection.setPos2(event.getClickedBlock().getLocation());
            MessageUtil.send(player, plugin.getConfig(), "selection-second-point",
                new String[]{"%x%", "%y%", "%z%"},
                new String[]{
                    String.valueOf(event.getClickedBlock().getX()),
                    String.valueOf(event.getClickedBlock().getY()),
                    String.valueOf(event.getClickedBlock().getZ())
                });
            
            // Show helper if selection is complete
            if (selection.isComplete()) {
                player.sendMessage(MessageUtil.colorize(plugin.getConfig().getString("messages.prefix") + 
                    "&7Use &e/cregioncreate &7to create a region"));
            }
        }
    }
}
