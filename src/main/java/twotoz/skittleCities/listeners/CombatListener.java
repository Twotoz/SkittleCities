package twotoz.skittleCities.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import twotoz.skittleCities.SkittleCities;

public class CombatListener implements Listener {
    private final SkittleCities plugin;

    public CombatListener(SkittleCities plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        // Track combat for teleport cooldown
        if (event.getEntity() instanceof Player victim) {
            plugin.getCombatManager().enterCombat(victim.getUniqueId());
        }
        
        if (event.getDamager() instanceof Player attacker) {
            plugin.getCombatManager().enterCombat(attacker.getUniqueId());
        }
    }
}
