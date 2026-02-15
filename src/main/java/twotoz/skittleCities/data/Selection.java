package twotoz.skittleCities.data;

import org.bukkit.Location;

public class Selection {
    private Location pos1;
    private Location pos2;

    public Selection() {
        this.pos1 = null;
        this.pos2 = null;
    }

    public boolean isComplete() {
        return pos1 != null && pos2 != null;
    }

    public void clear() {
        this.pos1 = null;
        this.pos2 = null;
    }

    // Getters and Setters
    public Location getPos1() { return pos1; }
    public void setPos1(Location pos1) { this.pos1 = pos1; }

    public Location getPos2() { return pos2; }
    public void setPos2(Location pos2) { this.pos2 = pos2; }
}
