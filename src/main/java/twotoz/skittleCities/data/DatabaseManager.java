package twotoz.skittleCities.data;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import twotoz.skittleCities.SkittleCities;

import java.io.File;
import java.sql.*;
import java.util.*;

public class DatabaseManager {
    private final SkittleCities plugin;
    private Connection connection;

    public DatabaseManager(SkittleCities plugin) {
        this.plugin = plugin;
        connect();
        createTables();
    }

    private void connect() {
        try {
            File dataFolder = plugin.getDataFolder();
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }

            String url = "jdbc:sqlite:" + dataFolder + "/data.db";
            connection = DriverManager.getConnection(url);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createTables() {
        String regionsTable = "CREATE TABLE IF NOT EXISTS regions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "world TEXT NOT NULL," +
                "pos1_x DOUBLE NOT NULL," +
                "pos1_y DOUBLE NOT NULL," +
                "pos1_z DOUBLE NOT NULL," +
                "pos2_x DOUBLE NOT NULL," +
                "pos2_y DOUBLE NOT NULL," +
                "pos2_z DOUBLE NOT NULL," +
                "owner TEXT," +
                "type TEXT NOT NULL," +
                "price DOUBLE NOT NULL," +
                "lease_days INTEGER NOT NULL," +
                "lease_expiry BIGINT NOT NULL," +
                "auto_extend BOOLEAN NOT NULL," +
                "sign_x DOUBLE," +
                "sign_y DOUBLE," +
                "sign_z DOUBLE" +
                ")";

        String flagsTable = "CREATE TABLE IF NOT EXISTS region_flags (" +
                "region_id INTEGER NOT NULL," +
                "flag_name TEXT NOT NULL," +
                "flag_value BOOLEAN NOT NULL," +
                "FOREIGN KEY(region_id) REFERENCES regions(id) ON DELETE CASCADE" +
                ")";

        String trustTable = "CREATE TABLE IF NOT EXISTS region_trust (" +
                "region_id INTEGER NOT NULL," +
                "player_uuid TEXT NOT NULL," +
                "FOREIGN KEY(region_id) REFERENCES regions(id) ON DELETE CASCADE" +
                ")";

        String balancesTable = "CREATE TABLE IF NOT EXISTS player_balances (" +
                "player_uuid TEXT PRIMARY KEY," +
                "balance DOUBLE NOT NULL" +
                ")";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(regionsTable);
            stmt.execute(flagsTable);
            stmt.execute(trustTable);
            stmt.execute(balancesTable);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Region operations
    public void saveRegion(Region region) {
        String sql = "INSERT INTO regions (name, world, pos1_x, pos1_y, pos1_z, pos2_x, pos2_y, pos2_z, " +
                "owner, type, price, lease_days, lease_expiry, auto_extend, sign_x, sign_y, sign_z) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, region.getName());
            pstmt.setString(2, region.getWorld().getName());
            pstmt.setDouble(3, region.getPos1().getX());
            pstmt.setDouble(4, region.getPos1().getY());
            pstmt.setDouble(5, region.getPos1().getZ());
            pstmt.setDouble(6, region.getPos2().getX());
            pstmt.setDouble(7, region.getPos2().getY());
            pstmt.setDouble(8, region.getPos2().getZ());
            pstmt.setString(9, region.getOwner() != null ? region.getOwner().toString() : null);
            pstmt.setString(10, region.getType().name());
            pstmt.setDouble(11, region.getPrice());
            pstmt.setInt(12, region.getLeaseDays());
            pstmt.setLong(13, region.getLeaseExpiry());
            pstmt.setBoolean(14, region.isAutoExtend());
            
            if (region.getSignLocation() != null) {
                pstmt.setDouble(15, region.getSignLocation().getX());
                pstmt.setDouble(16, region.getSignLocation().getY());
                pstmt.setDouble(17, region.getSignLocation().getZ());
            } else {
                pstmt.setNull(15, Types.DOUBLE);
                pstmt.setNull(16, Types.DOUBLE);
                pstmt.setNull(17, Types.DOUBLE);
            }

            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    region.setId(rs.getInt(1));
                    saveRegionFlags(region);
                    saveRegionTrust(region);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateRegion(Region region) {
        String sql = "UPDATE regions SET owner = ?, type = ?, price = ?, lease_days = ?, " +
                "lease_expiry = ?, auto_extend = ?, sign_x = ?, sign_y = ?, sign_z = ? WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, region.getOwner() != null ? region.getOwner().toString() : null);
            pstmt.setString(2, region.getType().name());
            pstmt.setDouble(3, region.getPrice());
            pstmt.setInt(4, region.getLeaseDays());
            pstmt.setLong(5, region.getLeaseExpiry());
            pstmt.setBoolean(6, region.isAutoExtend());
            
            if (region.getSignLocation() != null) {
                pstmt.setDouble(7, region.getSignLocation().getX());
                pstmt.setDouble(8, region.getSignLocation().getY());
                pstmt.setDouble(9, region.getSignLocation().getZ());
            } else {
                pstmt.setNull(7, Types.DOUBLE);
                pstmt.setNull(8, Types.DOUBLE);
                pstmt.setNull(9, Types.DOUBLE);
            }
            
            pstmt.setInt(10, region.getId());
            pstmt.executeUpdate();

            // Update flags and trust
            deleteRegionFlags(region.getId());
            deleteRegionTrust(region.getId());
            saveRegionFlags(region);
            saveRegionTrust(region);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void saveRegionFlags(Region region) {
        String sql = "INSERT INTO region_flags (region_id, flag_name, flag_value) VALUES (?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (Map.Entry<String, Boolean> entry : region.getFlags().entrySet()) {
                pstmt.setInt(1, region.getId());
                pstmt.setString(2, entry.getKey());
                pstmt.setBoolean(3, entry.getValue());
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void saveRegionTrust(Region region) {
        String sql = "INSERT INTO region_trust (region_id, player_uuid) VALUES (?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (UUID uuid : region.getTrustedPlayers()) {
                pstmt.setInt(1, region.getId());
                pstmt.setString(2, uuid.toString());
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteRegionFlags(int regionId) {
        String sql = "DELETE FROM region_flags WHERE region_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, regionId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteRegionTrust(int regionId) {
        String sql = "DELETE FROM region_trust WHERE region_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, regionId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Region> loadAllRegions() {
        List<Region> regions = new ArrayList<>();
        String sql = "SELECT * FROM regions";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Region region = buildRegionFromResultSet(rs);
                loadRegionFlags(region);
                loadRegionTrust(region);
                regions.add(region);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return regions;
    }

    private Region buildRegionFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        World world = plugin.getServer().getWorld(rs.getString("world"));
        
        if (world == null) {
            throw new SQLException("World not found: " + rs.getString("world"));
        }

        Location pos1 = new Location(world, rs.getDouble("pos1_x"), rs.getDouble("pos1_y"), rs.getDouble("pos1_z"));
        Location pos2 = new Location(world, rs.getDouble("pos2_x"), rs.getDouble("pos2_y"), rs.getDouble("pos2_z"));

        Region region = new Region(id, name, world, pos1, pos2);
        
        String ownerStr = rs.getString("owner");
        if (ownerStr != null) {
            region.setOwner(UUID.fromString(ownerStr));
        }
        
        region.setType(Region.RegionType.valueOf(rs.getString("type")));
        region.setPrice(rs.getDouble("price"));
        region.setLeaseDays(rs.getInt("lease_days"));
        region.setLeaseExpiry(rs.getLong("lease_expiry"));
        region.setAutoExtend(rs.getBoolean("auto_extend"));

        Double signX = rs.getObject("sign_x", Double.class);
        if (signX != null) {
            Location signLoc = new Location(world, signX, rs.getDouble("sign_y"), rs.getDouble("sign_z"));
            region.setSignLocation(signLoc);
        }

        return region;
    }

    private void loadRegionFlags(Region region) {
        String sql = "SELECT flag_name, flag_value FROM region_flags WHERE region_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, region.getId());
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                region.getFlags().put(rs.getString("flag_name"), rs.getBoolean("flag_value"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadRegionTrust(Region region) {
        String sql = "SELECT player_uuid FROM region_trust WHERE region_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, region.getId());
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                region.getTrustedPlayers().add(UUID.fromString(rs.getString("player_uuid")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Balance operations
    public double getBalance(UUID uuid) {
        String sql = "SELECT balance FROM player_balances WHERE player_uuid = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getDouble("balance");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return plugin.getConfig().getDouble("default-balance", 1000);
    }

    public void setBalance(UUID uuid, double balance) {
        String sql = "INSERT OR REPLACE INTO player_balances (player_uuid, balance) VALUES (?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            pstmt.setDouble(2, balance);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteRegion(int regionId) {
        String sql = "DELETE FROM regions WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, regionId);
            pstmt.executeUpdate();
            
            // Cascading deletes will handle flags and trust
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
