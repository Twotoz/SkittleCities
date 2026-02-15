package twotoz.skittleCities;

import org.bukkit.plugin.java.JavaPlugin;
import twotoz.skittleCities.commands.*;
import twotoz.skittleCities.data.DatabaseManager;
import twotoz.skittleCities.listeners.ClaimMoveListener;
import twotoz.skittleCities.listeners.CommandBlockListener;
import twotoz.skittleCities.listeners.PlayerJoinListener;
import twotoz.skittleCities.listeners.ProtectionListener;
import twotoz.skittleCities.listeners.SelectionListener;
import twotoz.skittleCities.listeners.SignListener;
import twotoz.skittleCities.managers.*;
import twotoz.skittleCities.tasks.LeaseCheckTask;
import twotoz.skittleCities.tasks.SignCheckTask;
import twotoz.skittleCities.tasks.StatusBarTask;

public final class SkittleCities extends JavaPlugin {
    
    private DatabaseManager databaseManager;
    private RegionManager regionManager;
    private EconomyManager economyManager;
    private FlagManager flagManager;
    private TrustManager trustManager;
    private SelectionManager selectionManager;
    private ActionBarManager actionBarManager;
    
    private DebugToolCommand debugToolCommand;
    private IgnoreClaimsCommand ignoreClaimsCommand;
    private ClaimMoveListener claimMoveListener;

    @Override
    public void onEnable() {
        // Save default config
        saveDefaultConfig();

        // Initialize managers
        databaseManager = new DatabaseManager(this);
        regionManager = new RegionManager(this);
        economyManager = new EconomyManager(this);
        flagManager = new FlagManager(this);
        trustManager = new TrustManager(this);
        selectionManager = new SelectionManager();
        actionBarManager = new ActionBarManager(this);

        // Load regions from database
        regionManager.loadRegions();

        // Register commands
        getCommand("ctool").setExecutor(new ToolCommand(this));
        getCommand("cregioncreate").setExecutor(new RegionCreateCommand(this));
        getCommand("cflags").setExecutor(new FlagsCommand(this));
        
        TrustCommand trustCommand = new TrustCommand(this);
        getCommand("ctrust").setExecutor(trustCommand);
        getCommand("ctrust").setTabCompleter(trustCommand);
        
        UntrustCommand untrustCommand = new UntrustCommand(this);
        getCommand("cuntrust").setExecutor(untrustCommand);
        getCommand("cuntrust").setTabCompleter(untrustCommand);
        
        getCommand("cbal").setExecutor(new BalanceCommand(this));
        
        getCommand("cautoextend").setExecutor(new AutoExtendCommand(this));
        
        getCommand("cclaims").setExecutor(new ClaimsCommand(this));
        
        getCommand("city").setExecutor(new CityCommand(this));
        getCommand("setcityspawn").setExecutor(new SetCitySpawnCommand(this));
        
        EconomyCommand economyCommand = new EconomyCommand(this);
        getCommand("ceconomy").setExecutor(economyCommand);
        getCommand("ceconomy").setTabCompleter(economyCommand);
        
        getCommand("cadmin").setExecutor(new AdminClaimsCommand(this));
        
        debugToolCommand = new DebugToolCommand(this);
        getCommand("cdebugtool").setExecutor(debugToolCommand);
        
        ignoreClaimsCommand = new IgnoreClaimsCommand(this);
        getCommand("cignoreclaims").setExecutor(ignoreClaimsCommand);

        // Register listeners
        getServer().getPluginManager().registerEvents(new ProtectionListener(this), this);
        getServer().getPluginManager().registerEvents(new SelectionListener(this), this);
        getServer().getPluginManager().registerEvents(new SignListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new CommandBlockListener(this), this);
        
        claimMoveListener = new ClaimMoveListener(this);
        getServer().getPluginManager().registerEvents(claimMoveListener, this);

        // Start lease check task
        int intervalMinutes = getConfig().getInt("lease-check-interval", 5);
        LeaseCheckTask leaseTask = new LeaseCheckTask(this);
        leaseTask.runTaskTimer(this, 20L * 60L * intervalMinutes, 20L * 60L * intervalMinutes);

        // Start status bar task (updates every 1 second)
        StatusBarTask statusBarTask = new StatusBarTask(this);
        statusBarTask.runTaskTimer(this, 20L, 20L);

        // Start sign check task (configurable interval)
        int signCheckInterval = getConfig().getInt("sign-check-interval", 30);
        SignCheckTask signCheckTask = new SignCheckTask(this);
        signCheckTask.runTaskTimer(this, 20L * signCheckInterval, 20L * signCheckInterval);

        getLogger().info("SkittleCities has been enabled!");
    }

    @Override
    public void onDisable() {
        // Close database connection
        if (databaseManager != null) {
            databaseManager.close();
        }

        getLogger().info("SkittleCities has been disabled!");
    }

    // Getters
    public DatabaseManager getDatabaseManager() { return databaseManager; }
    public RegionManager getRegionManager() { return regionManager; }
    public EconomyManager getEconomyManager() { return economyManager; }
    public FlagManager getFlagManager() { return flagManager; }
    public TrustManager getTrustManager() { return trustManager; }
    public SelectionManager getSelectionManager() { return selectionManager; }
    public ActionBarManager getActionBarManager() { return actionBarManager; }
    public DebugToolCommand getDebugToolCommand() { return debugToolCommand; }
    public IgnoreClaimsCommand getIgnoreClaimsCommand() { return ignoreClaimsCommand; }
    public ClaimMoveListener getClaimMoveListener() { return claimMoveListener; }
}
