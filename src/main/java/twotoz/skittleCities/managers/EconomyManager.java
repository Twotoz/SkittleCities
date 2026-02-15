package twotoz.skittleCities.managers;

import twotoz.skittleCities.SkittleCities;

import java.util.UUID;

public class EconomyManager {
    private final SkittleCities plugin;
    private final BalanceCache balanceCache;

    public EconomyManager(SkittleCities plugin) {
        this.plugin = plugin;
        this.balanceCache = new BalanceCache(plugin);
    }

    public double getBalance(UUID uuid) {
        return balanceCache.getBalance(uuid);
    }

    public void setBalance(UUID uuid, double balance) {
        balanceCache.setBalance(uuid, balance);
        notifyBalanceChange(uuid);
    }

    public void addBalance(UUID uuid, double amount) {
        balanceCache.addBalance(uuid, amount);
        notifyBalanceChange(uuid);
    }

    public void removeBalance(UUID uuid, double amount) {
        balanceCache.removeBalance(uuid, amount);
        notifyBalanceChange(uuid);
    }

    public boolean hasBalance(UUID uuid, double amount) {
        return getBalance(uuid) >= amount;
    }

    public boolean withdraw(UUID uuid, double amount) {
        if (!hasBalance(uuid, amount)) {
            return false;
        }
        removeBalance(uuid, amount);
        return true;
    }

    public void preloadBalance(UUID uuid) {
        balanceCache.preload(uuid);
    }
    
    private void notifyBalanceChange(UUID uuid) {
        org.bukkit.entity.Player player = plugin.getServer().getPlayer(uuid);
        if (player != null && player.isOnline() && plugin.getStatusBarListener() != null) {
            plugin.getStatusBarListener().onBalanceChange(player);
        }
    }
}
