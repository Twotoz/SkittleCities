package twotoz.skittleCities;

import org.bukkit.plugin.java.JavaPlugin;
import twotoz.skittleCities.commands.*;
import twotoz.skittleCities.data.DatabaseManager;
import twotoz.skittleCities.gui.HelpGUI;
import twotoz.skittleCities.gui.MainMenuGUI;
import twotoz.skittleCities.listeners.ClaimMoveListener;
import twotoz.skittleCities.listeners.CombatListener;
import twotoz.skittleCities.listeners.CommandBlockListener;
import twotoz.skittleCities.listeners.PlayerJoinListener;
import twotoz.skittleCities.listeners.ProtectionListener;
import twotoz.skittleCities.listeners.SelectionListener;
import twotoz.skittleCities.listeners.SignListener;
import twotoz.skittleCities.listeners.StatusBarListener;
import twotoz.skittleCities.listeners.WorldChangeListener;
import twotoz.skittleCities.managers.*;
import twotoz.skittleCities.tasks.LeaseCheckTask;
import twotoz.skittleCities.tasks.SignCheckTask;

public final class SkittleCities extends JavaPlugin {
    
    private DatabaseManager databaseManager;
    private RegionManager regionManager;
    private EconomyManager economyManager;
    private FlagManager flagManager;
    private TrustManager trustManager;
    private SelectionManager selectionManager;
    private ActionBarManager actionBarManager;
    private InventoryManager inventoryManager;
    private CombatManager combatManager;
    private SellSignManager sellSignManager;
    
    private DebugToolCommand debugToolCommand;
    private IgnoreClaimsCommand ignoreClaimsCommand;
    private ClaimMoveListener claimMoveListener;
    private CommandBlockListener commandBlockListener;
    private StatusBarListener statusBarListener;

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
        inventoryManager = new InventoryManager(this);
        combatManager = new CombatManager(this);
        sellSignManager = new SellSignManager(this);

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
        getCommand("cmenu").setExecutor(new MenuCommand(this));
        getCommand("leavecity").setExecutor(new LeaveCityCommand(this));
        
        CreateSellSignCommand sellSignCommand = new CreateSellSignCommand(this);
        getCommand("csellsign").setExecutor(sellSignCommand);
        getCommand("csellsign").setTabCompleter(sellSignCommand);
        
        getCommand("crename").setExecutor(new RenameClaimCommand(this));
        getCommand("csubclaim").setExecutor(new CreateSubclaimCommand(this));
        getCommand("csubclaims").setExecutor(new SubclaimsCommand(this));
        
        debugToolCommand = new DebugToolCommand(this);
        getCommand("cdebugtool").setExecutor(debugToolCommand);
        
        ignoreClaimsCommand = new IgnoreClaimsCommand(this);
        getCommand("cignoreclaims").setExecutor(ignoreClaimsCommand);

        // Register listeners
        getServer().getPluginManager().registerEvents(new ProtectionListener(this), this);
        getServer().getPluginManager().registerEvents(new SelectionListener(this), this);
        getServer().getPluginManager().registerEvents(new SignListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new WorldChangeListener(this), this);
        getServer().getPluginManager().registerEvents(new CombatListener(this), this);
        getServer().getPluginManager().registerEvents(new MainMenuGUI(this), this);
        getServer().getPluginManager().registerEvents(new HelpGUI(this), this);
        
        commandBlockListener = new CommandBlockListener(this);
        getServer().getPluginManager().registerEvents(commandBlockListener, this);
        
        claimMoveListener = new ClaimMoveListener(this);
        getServer().getPluginManager().registerEvents(claimMoveListener, this);
        
        statusBarListener = new StatusBarListener(this);
        getServer().getPluginManager().registerEvents(statusBarListener, this);
        
        getCommand("citycommandbypass").setExecutor(new CityCommandBypassCommand(this));

        // Start lease check task - Folia compatible
        int intervalMinutes = getConfig().getInt("lease-check-interval", 5);
        LeaseCheckTask leaseTask = new LeaseCheckTask(this);
        twotoz.skittleCities.utils.SchedulerUtil.runTaskTimer(this, leaseTask::run, 
            20L * 60L * intervalMinutes, 20L * 60L * intervalMinutes);

        // Start sign check task (configurable interval) - Folia compatible
        int signCheckInterval = getConfig().getInt("sign-check-interval", 30);
        SignCheckTask signCheckTask = new SignCheckTask(this);
        twotoz.skittleCities.utils.SchedulerUtil.runTaskTimer(this, signCheckTask::run, 
            20L * signCheckInterval, 20L * signCheckInterval);

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
    public InventoryManager getInventoryManager() { return inventoryManager; }
    public CombatManager getCombatManager() { return combatManager; }
    public SellSignManager getSellSignManager() { return sellSignManager; }
    public DebugToolCommand getDebugToolCommand() { return debugToolCommand; }
    public IgnoreClaimsCommand getIgnoreClaimsCommand() { return ignoreClaimsCommand; }
    public ClaimMoveListener getClaimMoveListener() { return claimMoveListener; }
    public CommandBlockListener getCommandBlockListener() { return commandBlockListener; }
    public StatusBarListener getStatusBarListener() { return statusBarListener; }
}
