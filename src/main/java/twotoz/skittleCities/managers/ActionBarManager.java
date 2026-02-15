package twotoz.skittleCities.managers;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import twotoz.skittleCities.SkittleCities;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ActionBarManager {
    private final SkittleCities plugin;
    private final Map<UUID, Long> temporaryMessages;
    
    public ActionBarManager(SkittleCities plugin) {
        this.plugin = plugin;
        this.temporaryMessages = new HashMap<>();
    }

    /**
     * Send a temporary action bar message (shown for ~2 seconds)
     */
    public void sendTemporary(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
        temporaryMessages.put(player.getUniqueId(), System.currentTimeMillis() + 2000); // 2 seconds
    }

    /**
     * Check if a temporary message is still active
     */
    public boolean hasTemporaryMessage(UUID playerId) {
        Long expiry = temporaryMessages.get(playerId);
        if (expiry == null) return false;
        
        if (System.currentTimeMillis() > expiry) {
            temporaryMessages.remove(playerId);
            return false;
        }
        return true;
    }

    /**
     * Send the persistent status bar (if no temporary message is active)
     */
    public void sendPersistent(Player player, String message) {
        if (!hasTemporaryMessage(player.getUniqueId())) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
        }
    }
}
