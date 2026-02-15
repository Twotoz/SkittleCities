package twotoz.skittleCities.data;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.*;

public class Region {
    private int id;
    private String name;
    private World world;
    private Location pos1;
    private Location pos2;
    private UUID owner;
    private RegionType type;
    private double price;
    private int leaseDays;
    private long leaseExpiry; // Timestamp in millis
    private boolean autoExtend;
    private Map<String, Boolean> flags;
    private Set<UUID> trustedPlayers;
    private Location signLocation;

    public enum RegionType {
        FOR_HIRE,    // Lease system
        FOR_SALE,    // Permanent purchase
        PRIVATE,     // Admin preset, no sign
        SAFEZONE     // Admin safe area (spawn, etc)
    }

    public Region(int id, String name, World world, Location pos1, Location pos2) {
        this.id = id;
        this.name = name;
        this.world = world;
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.flags = new HashMap<>();
        this.trustedPlayers = new HashSet<>();
        this.type = RegionType.PRIVATE;
        this.price = 0;
        this.leaseDays = 0;
        this.leaseExpiry = 0;
        this.autoExtend = false;
    }

    public boolean contains(Location loc) {
        if (!loc.getWorld().equals(world)) return false;
        
        // Use block coordinates for accurate boundary checks
        int minBlockX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int maxBlockX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int minBlockY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int maxBlockY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int minBlockZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxBlockZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

        return loc.getBlockX() >= minBlockX && loc.getBlockX() <= maxBlockX &&
               loc.getBlockY() >= minBlockY && loc.getBlockY() <= maxBlockY &&
               loc.getBlockZ() >= minBlockZ && loc.getBlockZ() <= maxBlockZ;
    }

    public boolean overlaps(Region other) {
        if (!world.equals(other.world)) return false;

        // Use block coordinates for accurate overlap checks
        int minBlockX1 = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int maxBlockX1 = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int minBlockY1 = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int maxBlockY1 = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int minBlockZ1 = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxBlockZ1 = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

        int minBlockX2 = Math.min(other.pos1.getBlockX(), other.pos2.getBlockX());
        int maxBlockX2 = Math.max(other.pos1.getBlockX(), other.pos2.getBlockX());
        int minBlockY2 = Math.min(other.pos1.getBlockY(), other.pos2.getBlockY());
        int maxBlockY2 = Math.max(other.pos1.getBlockY(), other.pos2.getBlockY());
        int minBlockZ2 = Math.min(other.pos1.getBlockZ(), other.pos2.getBlockZ());
        int maxBlockZ2 = Math.max(other.pos1.getBlockZ(), other.pos2.getBlockZ());

        return maxBlockX1 >= minBlockX2 && minBlockX1 <= maxBlockX2 &&
               maxBlockY1 >= minBlockY2 && minBlockY1 <= maxBlockY2 &&
               maxBlockZ1 >= minBlockZ2 && minBlockZ1 <= maxBlockZ2;
    }

    public Location getCenter() {
        double centerX = (pos1.getX() + pos2.getX()) / 2;
        double centerY = (pos1.getY() + pos2.getY()) / 2;
        double centerZ = (pos1.getZ() + pos2.getZ()) / 2;
        return new Location(world, centerX, centerY, centerZ);
    }

    public boolean isExpired() {
        if (type != RegionType.FOR_HIRE || leaseExpiry == 0) return false;
        return System.currentTimeMillis() > leaseExpiry;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public World getWorld() { return world; }
    public void setWorld(World world) { this.world = world; }

    public Location getPos1() { return pos1; }
    public void setPos1(Location pos1) { this.pos1 = pos1; }

    public Location getPos2() { return pos2; }
    public void setPos2(Location pos2) { this.pos2 = pos2; }

    public UUID getOwner() { return owner; }
    public void setOwner(UUID owner) { this.owner = owner; }

    public RegionType getType() { return type; }
    public void setType(RegionType type) { this.type = type; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getLeaseDays() { return leaseDays; }
    public void setLeaseDays(int leaseDays) { this.leaseDays = leaseDays; }

    public long getLeaseExpiry() { return leaseExpiry; }
    public void setLeaseExpiry(long leaseExpiry) { this.leaseExpiry = leaseExpiry; }

    public boolean isAutoExtend() { return autoExtend; }
    public void setAutoExtend(boolean autoExtend) { this.autoExtend = autoExtend; }

    public Map<String, Boolean> getFlags() { return flags; }
    public void setFlags(Map<String, Boolean> flags) { this.flags = flags; }

    public Set<UUID> getTrustedPlayers() { return trustedPlayers; }
    public void setTrustedPlayers(Set<UUID> trustedPlayers) { this.trustedPlayers = trustedPlayers; }

    public Location getSignLocation() { return signLocation; }
    public void setSignLocation(Location signLocation) { this.signLocation = signLocation; }
}
