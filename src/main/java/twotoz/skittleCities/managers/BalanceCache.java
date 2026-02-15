package twotoz.skittleCities.managers;

import twotoz.skittleCities.SkittleCities;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BalanceCache {
    private final SkittleCities plugin;
    private final Map<UUID, Double> cache;

    public BalanceCache(SkittleCities plugin) {
        this.plugin = plugin;
        this.cache = new HashMap<>();
    }

    public double getBalance(UUID uuid) {
        return cache.computeIfAbsent(uuid, id -> 
            plugin.getDatabaseManager().getBalance(id));
    }

    public void setBalance(UUID uuid, double balance) {
        cache.put(uuid, balance);
        plugin.getDatabaseManager().setBalance(uuid, balance);
    }

    public void addBalance(UUID uuid, double amount) {
        double current = getBalance(uuid);
        setBalance(uuid, current + amount);
    }

    public void removeBalance(UUID uuid, double amount) {
        double current = getBalance(uuid);
        setBalance(uuid, current - amount);
    }

    public void invalidate(UUID uuid) {
        cache.remove(uuid);
    }

    public void invalidateAll() {
        cache.clear();
    }

    public void preload(UUID uuid) {
        getBalance(uuid); // Load into cache
    }
}
