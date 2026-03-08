package com.soloarise.plugin;

import com.soloarise.plugin.commands.*;
import com.soloarise.plugin.commands.admin.AdminPanelCommand;
import com.soloarise.plugin.commands.tabcompleters.AdminTabCompleter;
import com.soloarise.plugin.commands.tabcompleters.SummonTabCompleter;
import com.soloarise.plugin.listeners.*;
import com.soloarise.plugin.managers.*;
import org.bukkit.plugin.java.JavaPlugin;

public class SoloArisePlugin extends JavaPlugin {
    
    private static SoloArisePlugin instance;
    
    // Managers
    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private PlayerManager playerManager;
    private SoulManager soulManager;
    private TaskManager taskManager;
    private ScoreboardManager scoreboardManager;
    private ParticleManager particleManager;
    private SummonSessionManager summonSessionManager;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // Initialize configurations
        saveDefaultConfig();
        configManager = new ConfigManager(this);
        
        // Initialize managers
        databaseManager = new DatabaseManager(this);
        playerManager = new PlayerManager(this);
        soulManager = new SoulManager(this);
        taskManager = new TaskManager(this);
        scoreboardManager = new ScoreboardManager(this);
        particleManager = new ParticleManager(this);
        summonSessionManager = new SummonSessionManager(this);
        
        // Register commands
        registerCommands();
        
        // Register listeners
        registerListeners();
        
        // Start scheduler for boss bar updates
        startBossBarScheduler();
        
        getLogger().info("SoloArisePlugin has been enabled!");
        getLogger().info("Version: 1.0.0");
        getLogger().info("Made for Minecraft 1.21.4");
    }
    
    @Override
    public void onDisable() {
        // Save all player data
        if (databaseManager != null) {
            databaseManager.saveAll();
        }
        
        // Recall all summoned souls
        if (soulManager != null) {
            soulManager.recallAllSouls();
        }
        
        getLogger().info("SoloArisePlugin has been disabled!");
    }
    
    private void registerCommands() {
        // Main commands
        getCommand("arise").setExecutor(new AriseCommand(this));
        getCommand("arisework").setExecutor(new AriseWorkCommand(this));
        getCommand("soulrelease").setExecutor(new SoulReleaseCommand(this));
        getCommand("soulattack").setExecutor(new SoulAttackCommand(this));
        getCommand("soulcome").setExecutor(new SoulComeCommand(this));
        getCommand("soultask").setExecutor(new SoulTaskCommand(this));
        getCommand("summon").setExecutor(new SummonCommand(this));
        getCommand("healsoul").setExecutor(new HealSoulCommand(this));
        getCommand("solarise").setExecutor(new AdminPanelCommand(this));
        
        // Register tab completers
        getCommand("summon").setTabCompleter(new SummonTabCompleter(this));
        getCommand("solarise").setTabCompleter(new AdminTabCompleter(this));
    }
    
    private void registerListeners() {
        // Player listeners
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerMoveListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerChatListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerInteractListener(this), this);
        
        // Entity listeners
        getServer().getPluginManager().registerEvents(new MobDeathListener(this), this);
        getServer().getPluginManager().registerEvents(new EntityDamageListener(this), this);
        getServer().getPluginManager().registerEvents(new EntityTargetListener(this), this);
        
        // World listeners
        getServer().getPluginManager().registerEvents(new WorldListener(this), this);
        
        // Inventory listeners
        getServer().getPluginManager().registerEvents(new InventoryListener(this), this);
    }
    
    private void startBossBarScheduler() {
        // Update boss bars every second
        getServer().getScheduler().runTaskTimer(this, () -> {
            if (taskManager != null) {
                taskManager.updateAllBossBars();
            }
        }, 20L, 20L);
        
        // Update scoreboards every 2 seconds
        getServer().getScheduler().runTaskTimer(this, () -> {
            if (scoreboardManager != null) {
                scoreboardManager.updateAllScoreboards();
            }
        }, 40L, 40L);
        
        // Check for expired tasks every minute
        getServer().getScheduler().runTaskTimer(this, () -> {
            if (playerManager != null) {
                playerManager.checkExpiredTasks();
            }
        }, 1200L, 1200L);
    }
    
    // Getters for all managers
    public static SoloArisePlugin getInstance() {
        return instance;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
    
    public PlayerManager getPlayerManager() {
        return playerManager;
    }
    
    public SoulManager getSoulManager() {
        return soulManager;
    }
    
    public TaskManager getTaskManager() {
        return taskManager;
    }
    
    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }
    
    public ParticleManager getParticleManager() {
        return particleManager;
    }
    
    public SummonSessionManager getSummonSessionManager() {
        return summonSessionManager;
    }
}
