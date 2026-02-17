package twotoz.skittleCities.managers;

import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import twotoz.skittleCities.SkittleCities;
import twotoz.skittleCities.utils.MessageUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SellSignManager {
    private final SkittleCities plugin;
    private final Map<UUID, Map<Integer, PendingBatch>> pendingBatches = new HashMap<>();

    public SellSignManager(SkittleCities plugin) {
        this.plugin = plugin;
    }

    private static class PendingBatch {
        int amount = 0;
        double total = 0;
        String materialName = "";
        boolean isSell;
        org.bukkit.scheduler.BukkitTask task = null;
    }

    public boolean isSellSign(Sign sign) {
        if (sign.getLine(0).isEmpty()) return false;
        String line0 = org.bukkit.ChatColor.stripColor(sign.getLine(0));
        return line0.equalsIgnoreCase("[SELL]") || line0.equalsIgnoreCase("[BUY]");
    }

    public void createSign(Sign sign, Material material, double price, boolean isBuySign) {
        String signType = isBuySign ? "[buy]" : "[sell]";
        sign.setLine(0, MessageUtil.colorize("&9&l" + signType));
        sign.setLine(1, MessageUtil.colorize("&e" + formatMaterialName(material)));
        sign.setLine(2, MessageUtil.colorize("&6$" + String.format("%.2f", price) + " &7each"));
        sign.setLine(3, MessageUtil.colorize("&7Total: &e0"));
        sign.update();
    }

    private String formatMaterialName(Material m) {
        return m.name().toLowerCase().replace("_", " ");
    }

    public Material getMaterial(Sign sign) {
        try {
            String name = org.bukkit.ChatColor.stripColor(sign.getLine(1)).trim().toUpperCase().replace(" ", "_");
            return Material.valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public double getPrice(Sign sign) {
        try {
            String line = org.bukkit.ChatColor.stripColor(sign.getLine(2));
            line = line.replace("$", "").replace("each", "").trim();
            return Double.parseDouble(line);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    /** Main entry point - called from SignListener */
    public void handleSign(Player player, Sign sign, boolean shiftClick) {
        String line0 = org.bukkit.ChatColor.stripColor(sign.getLine(0)).toLowerCase().trim();
        if (line0.equals("[sell]")) {
            handlePlayerSell(player, sign, shiftClick);
        } else if (line0.equals("[buy]")) {
            handlePlayerBuy(player, sign);
        }
    }

    // Keep old method name for compatibility with existing SignListener call
    public void handleSell(Player player, Sign sign) {
        handleSign(player, sign, false);
    }

    // ── [sell] sign: player SELLS items ──────────────────────────────────────

    private void handlePlayerSell(Player player, Sign sign, boolean shiftClick) {
        Material material = getMaterial(sign);
        if (material == null) {
            plugin.getActionBarManager().sendTemporary(player, MessageUtil.colorize("&cInvalid sign!"));
            return;
        }
        double price = getPrice(sign);
        if (price <= 0) {
            plugin.getActionBarManager().sendTemporary(player, MessageUtil.colorize("&cInvalid price!"));
            return;
        }

        int amount = shiftClick ? countItems(player, material) : 1;
        if (amount == 0) {
            plugin.getActionBarManager().sendTemporary(player,
                MessageUtil.colorize("&cNo &e" + formatMaterialName(material) + " &cto sell!"));
            return;
        }

        int removed = removeItems(player, material, amount);
        if (removed == 0) return;

        double earnings = removed * price;
        plugin.getEconomyManager().addBalance(player.getUniqueId(), earnings);
        updateSignStats(sign, removed);
        addToBatch(player, sign, removed, earnings, formatMaterialName(material), true);
    }

    // ── [buy] sign: player BUYS 1 item ───────────────────────────────────────

    private void handlePlayerBuy(Player player, Sign sign) {
        Material material = getMaterial(sign);
        if (material == null) {
            plugin.getActionBarManager().sendTemporary(player, MessageUtil.colorize("&cInvalid sign!"));
            return;
        }
        double price = getPrice(sign);
        if (price <= 0) {
            plugin.getActionBarManager().sendTemporary(player, MessageUtil.colorize("&cInvalid price!"));
            return;
        }

        double balance = plugin.getEconomyManager().getBalance(player.getUniqueId());
        if (balance < price) {
            plugin.getActionBarManager().sendTemporary(player,
                MessageUtil.colorize("&cNeed &6$" + String.format("%.2f", price) +
                    " &cbut have &6$" + String.format("%.2f", balance)));
            return;
        }

        // Check space for 1 item
        boolean hasSpace = false;
        for (ItemStack item : player.getInventory().getStorageContents()) {
            if (item == null || item.getType() == Material.AIR) { hasSpace = true; break; }
            if (item.getType() == material && item.getAmount() < item.getMaxStackSize()) { hasSpace = true; break; }
        }
        if (!hasSpace) {
            plugin.getActionBarManager().sendTemporary(player, MessageUtil.colorize("&cInventory full!"));
            return;
        }

        plugin.getEconomyManager().removeBalance(player.getUniqueId(), price);
        player.getInventory().addItem(new ItemStack(material, 1));
        updateSignStats(sign, 1);
        addToBatch(player, sign, 1, price, formatMaterialName(material), false);
    }

    // ── Smart batching ────────────────────────────────────────────────────────

    private void addToBatch(Player player, Sign sign, int amount, double total,
                            String materialName, boolean isSell) {
        UUID uuid = player.getUniqueId();
        int signHash = sign.getLocation().hashCode();

        pendingBatches.computeIfAbsent(uuid, k -> new HashMap<>());
        PendingBatch batch = pendingBatches.get(uuid).computeIfAbsent(signHash, k -> new PendingBatch());
        batch.amount += amount;
        batch.total += total;
        batch.materialName = materialName;
        batch.isSell = isSell;

        if (batch.task != null) batch.task.cancel();

        showBatchMessage(player, batch);

        final PendingBatch snap = batch;
        batch.task = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            showBatchMessage(player, snap);
            Map<Integer, PendingBatch> map = pendingBatches.get(uuid);
            if (map != null) {
                map.remove(signHash);
                if (map.isEmpty()) pendingBatches.remove(uuid);
            }
        }, 20L);
    }

    private void showBatchMessage(Player player, PendingBatch batch) {
        String msg = batch.isSell
            ? MessageUtil.colorize("&aSold &e" + batch.amount + "x " + batch.materialName +
                " &afor &6$" + String.format("%.2f", batch.total))
            : MessageUtil.colorize("&aBought &e" + batch.amount + "x " + batch.materialName +
                " &afor &6$" + String.format("%.2f", batch.total));
        plugin.getActionBarManager().sendTemporary(player, msg);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void updateSignStats(Sign sign, int amount) {
        try {
            String line = org.bukkit.ChatColor.stripColor(sign.getLine(3));
            int total = 0;
            if (line.contains(":")) {
                String[] parts = line.split(":");
                if (parts.length > 1) total = Integer.parseInt(parts[1].trim().replace(",", ""));
            }
            total += amount;
            sign.setLine(3, MessageUtil.colorize("&7Total: &e" + total));
            sign.update();
        } catch (Exception ignored) {}
    }

    private int countItems(Player player, Material material) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material) count += item.getAmount();
        }
        return count;
    }

    private int removeItems(Player player, Material material, int amount) {
        int remaining = amount;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material) {
                int toRemove = Math.min(remaining, item.getAmount());
                item.setAmount(item.getAmount() - toRemove);
                remaining -= toRemove;
                if (remaining == 0) break;
            }
        }
        return amount - remaining;
    }
}
