package twotoz.skittleCities.managers;

import org.bukkit.entity.Player;
import twotoz.skittleCities.data.Selection;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SelectionManager {
    private final Map<UUID, Selection> selections;

    public SelectionManager() {
        this.selections = new HashMap<>();
    }

    public Selection getSelection(Player player) {
        return selections.computeIfAbsent(player.getUniqueId(), k -> new Selection());
    }

    public void clearSelection(Player player) {
        Selection selection = getSelection(player);
        selection.clear();
    }

    public void removeSelection(Player player) {
        selections.remove(player.getUniqueId());
    }
}
