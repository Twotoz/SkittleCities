package twotoz.skittleCities.managers;

import org.bukkit.entity.Player;
import twotoz.skittleCities.SkittleCities;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CombatManager {
    private final SkittleCities plugin;
    private final Map<UUID, Long> combatLog;
    
    public CombatManager(SkittleCities plugin) {
        this.plugin = plugin;
        this.combatLog = new HashMap<>();
    }
    
    /**
     * Mark player as in combat
     */
    public void enterCombat(UUID playerId) {
        combatLog.put(playerId, System.currentTimeMillis());
    }
    
    /**
     * Check if player is in combat
     */
    public boolean isInCombat(UUID playerId) {
        if (!combatLog.containsKey(playerId)) return false;
        
        long lastCombat = combatLog.get(playerId);
        long cooldown = plugin.getConfig().getLong("combat-cooldown", 10) * 1000; // seconds to ms
        
        if (System.currentTimeMillis() - lastCombat > cooldown) {
            // Combat expired
            combatLog.remove(playerId);
            return false;
        }
        
        return true;
    }
    
    /**
     * Get remaining combat time in seconds
     */
    public int getRemainingCombatTime(UUID playerId) {
        if (!combatLog.containsKey(playerId)) return 0;
        
        long lastCombat = combatLog.get(playerId);
        long cooldown = plugin.getConfig().getLong("combat-cooldown", 10) * 1000;
        long remaining = cooldown - (System.currentTimeMillis() - lastCombat);
        
        return Math.max(0, (int) (remaining / 1000));
    }
    
    /**
     * Manually clear combat status
     */
    public void leaveCombat(UUID playerId) {
        combatLog.remove(playerId);
    }
    
    /**
     * Cleanup on player quit
     */
    public void cleanup(Player player) {
        combatLog.remove(player.getUniqueId());
    }
}
