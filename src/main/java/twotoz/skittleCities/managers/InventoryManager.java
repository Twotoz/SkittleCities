package twotoz.skittleCities.managers;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import twotoz.skittleCities.SkittleCities;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class InventoryManager {
    private final SkittleCities plugin;
    private final Map<UUID, ItemStack[]> cityInventories;
    private final Map<UUID, ItemStack[]> cityArmor;
    private final Map<UUID, ItemStack[]> otherWorldInventories;
    private final Map<UUID, ItemStack[]> otherWorldArmor;
    private final File inventoriesFolder;

    public InventoryManager(SkittleCities plugin) {
        this.plugin = plugin;
        this.cityInventories = new HashMap<>();
        this.cityArmor = new HashMap<>();
        this.otherWorldInventories = new HashMap<>();
        this.otherWorldArmor = new HashMap<>();
        
        // Create inventories folder
        this.inventoriesFolder = new File(plugin.getDataFolder(), "inventories");
        if (!inventoriesFolder.exists()) {
            inventoriesFolder.mkdirs();
        }
    }

    /**
     * Called when player enters city world
     */
    public void switchToCityInventory(Player player) {
        UUID uuid = player.getUniqueId();

        // Save current inventory (other world) to file - CRASH SAFE!
        saveInventoryToFile(uuid, false, player.getInventory().getContents(), player.getInventory().getArmorContents());
        otherWorldInventories.put(uuid, player.getInventory().getContents().clone());
        otherWorldArmor.put(uuid, player.getInventory().getArmorContents().clone());

        // Clear current inventory
        player.getInventory().clear();
        player.getInventory().setArmorContents(new ItemStack[4]);

        // Load city inventory from memory or file
        ItemStack[] cityInv = cityInventories.get(uuid);
        ItemStack[] cityArm = cityArmor.get(uuid);
        
        if (cityInv == null) {
            // Try loading from file
            ItemStack[][] loaded = loadInventoryFromFile(uuid, true);
            if (loaded != null) {
                cityInv = loaded[0];
                cityArm = loaded[1];
                cityInventories.put(uuid, cityInv);
                cityArmor.put(uuid, cityArm);
            }
        }
        
        if (cityInv != null) {
            player.getInventory().setContents(cityInv);
            player.getInventory().setArmorContents(cityArm);
        }

        player.updateInventory();
    }

    /**
     * Called when player leaves city world
     */
    public void switchToOtherWorldInventory(Player player) {
        UUID uuid = player.getUniqueId();

        // Save city inventory to file - CRASH SAFE!
        saveInventoryToFile(uuid, true, player.getInventory().getContents(), player.getInventory().getArmorContents());
        cityInventories.put(uuid, player.getInventory().getContents().clone());
        cityArmor.put(uuid, player.getInventory().getArmorContents().clone());

        // Clear current inventory
        player.getInventory().clear();
        player.getInventory().setArmorContents(new ItemStack[4]);

        // Load other world inventory
        if (otherWorldInventories.containsKey(uuid)) {
            player.getInventory().setContents(otherWorldInventories.get(uuid));
            player.getInventory().setArmorContents(otherWorldArmor.get(uuid));
        } else {
            // Try loading from file
            ItemStack[][] loaded = loadInventoryFromFile(uuid, false);
            if (loaded != null) {
                player.getInventory().setContents(loaded[0]);
                player.getInventory().setArmorContents(loaded[1]);
                otherWorldInventories.put(uuid, loaded[0]);
                otherWorldArmor.put(uuid, loaded[1]);
            }
        }

        player.updateInventory();
    }

    /**
     * Check if player is in city world
     */
    public boolean isInCityWorld(Player player) {
        String cityWorld = plugin.getConfig().getString("world-name");
        return player.getWorld().getName().equals(cityWorld);
    }

    /**
     * Save player inventory on quit - CRASH SAFE
     */
    public void saveInventoryOnQuit(Player player) {
        UUID uuid = player.getUniqueId();
        
        if (isInCityWorld(player)) {
            // Save city inventory
            saveInventoryToFile(uuid, true, player.getInventory().getContents(), player.getInventory().getArmorContents());
            cityInventories.put(uuid, player.getInventory().getContents().clone());
            cityArmor.put(uuid, player.getInventory().getArmorContents().clone());
        } else {
            // Save other world inventory
            saveInventoryToFile(uuid, false, player.getInventory().getContents(), player.getInventory().getArmorContents());
            otherWorldInventories.put(uuid, player.getInventory().getContents().clone());
            otherWorldArmor.put(uuid, player.getInventory().getArmorContents().clone());
        }
    }

    private void saveInventoryToFile(UUID uuid, boolean isCity, ItemStack[] inventory, ItemStack[] armor) {
        File file = new File(inventoriesFolder, uuid.toString() + (isCity ? "_city.inv" : "_other.inv"));
        
        try (FileOutputStream fos = new FileOutputStream(file);
             GZIPOutputStream gzos = new GZIPOutputStream(fos);
             BukkitObjectOutputStream oos = new BukkitObjectOutputStream(gzos)) {
            
            oos.writeObject(inventory);
            oos.writeObject(armor);
            oos.flush();
            
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save inventory for " + uuid + ": " + e.getMessage());
        }
    }

    private ItemStack[][] loadInventoryFromFile(UUID uuid, boolean isCity) {
        File file = new File(inventoriesFolder, uuid.toString() + (isCity ? "_city.inv" : "_other.inv"));
        
        if (!file.exists()) return null;
        
        try (FileInputStream fis = new FileInputStream(file);
             GZIPInputStream gzis = new GZIPInputStream(fis);
             BukkitObjectInputStream ois = new BukkitObjectInputStream(gzis)) {
            
            ItemStack[] inventory = (ItemStack[]) ois.readObject();
            ItemStack[] armor = (ItemStack[]) ois.readObject();
            
            return new ItemStack[][] { inventory, armor };
            
        } catch (IOException | ClassNotFoundException e) {
            plugin.getLogger().warning("Failed to load inventory for " + uuid + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Cleanup on player quit - CRITICAL: Remove from memory!
     */
    public void cleanup(Player player) {
        UUID uuid = player.getUniqueId();
        
        // Remove from memory maps to prevent memory leak
        cityInventories.remove(uuid);
        cityArmor.remove(uuid);
        otherWorldInventories.remove(uuid);
        otherWorldArmor.remove(uuid);
        
        // Inventories are already persisted to disk in saveInventoryOnQuit()
    }
}
