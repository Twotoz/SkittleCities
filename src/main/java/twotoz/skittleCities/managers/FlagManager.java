package twotoz.skittleCities.managers;

import org.bukkit.configuration.ConfigurationSection;
import twotoz.skittleCities.SkittleCities;
import twotoz.skittleCities.data.Region;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class FlagManager {
    private final SkittleCities plugin;
    private final Map<String, Boolean> worldFlags;

    public FlagManager(SkittleCities plugin) {
        this.plugin = plugin;
        this.worldFlags = new HashMap<>();
        loadWorldFlags();
    }

    private void loadWorldFlags() {
        ConfigurationSection flagsSection = plugin.getConfig().getConfigurationSection("default-world-flags");
        if (flagsSection != null) {
            for (String key : flagsSection.getKeys(false)) {
                worldFlags.put(key, flagsSection.getBoolean(key));
            }
        }
    }

    public Map<String, Boolean> getWorldFlags() {
        return new HashMap<>(worldFlags);
    }

    public void setWorldFlag(String flag, boolean value) {
        worldFlags.put(flag, value);
        plugin.getConfig().set("default-world-flags." + flag, value);
        plugin.saveConfig();
    }

    public boolean getWorldFlag(String flag) {
        return worldFlags.getOrDefault(flag, false);
    }

    public void setClaimFlag(Region region, String flag, boolean value) {
        region.getFlags().put(flag, value);
        plugin.getRegionManager().updateRegion(region);
    }

    public boolean getClaimFlag(Region region, String flag) {
        return region.getFlags().getOrDefault(flag, getDefaultClaimFlag(flag));
    }

    private boolean getDefaultClaimFlag(String flag) {
        return plugin.getConfig().getBoolean("default-claim-flags." + flag, false);
    }

    public void applyDefaultFlags(Region region) {
        ConfigurationSection flagsSection = plugin.getConfig().getConfigurationSection("default-claim-flags");
        if (flagsSection != null) {
            for (String key : flagsSection.getKeys(false)) {
                region.getFlags().put(key, flagsSection.getBoolean(key));
            }
        }
    }

    public Set<String> getAvailableFlags() {
        return worldFlags.keySet();
    }
}
