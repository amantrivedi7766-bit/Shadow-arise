package com.soloarise.plugin;

import org.bukkit.plugin.java.JavaPlugin;
import com.soloarise.plugin.listeners.*;
import com.soloarise.plugin.managers.*;
import com.soloarise.plugin.commands.*;

public class SoloArisePlugin extends JavaPlugin {
    
    private static SoloArisePlugin instance;
    private PlayerManager playerManager;
    private SoulManager soulManager;
    private TaskManager taskManager;
    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private ScoreboardManager scoreboardManager;
    private ParticleManager particleManager;
    
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
        
        // Register commands
        registerCommands();
        
        // Register listeners
        registerListeners();
        
        // Start scheduler for boss bar updates
        startBossBarScheduler();
        
        getLogger().info("SoloArisePlugin has been enabled!");
    }
    
    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.saveAll();
        }
        getLogger().info("SoloArisePlugin has been disabled!");
    }
    
    private void registerCommands() {
        getCommand("arise").setExecutor(new AriseCommand(this));
        getCommand("arisework").setExecutor(new AriseWorkCommand(this));
        getCommand("soulrelease").setExecutor(new SoulReleaseCommand(this));
        getCommand("soulattack").setExecutor(new SoulAttackCommand(this));
        getCommand("soulcome").setExecutor(new SoulComeCommand(this));
        getCommand("soultask").setExecutor(new SoulTaskCommand(this));
    }
    
    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(this), this);
        getServer().getPluginManager().registerEvents(new MobDeathListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerChatListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerMoveListener(this), this);
    }
    
    private void startBossBarScheduler() {
        getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
            if (taskManager != null) {
                taskManager.updateAllBossBars();
            }
        }, 0L, 20L);
    }
    
    public static SoloArisePlugin getInstance() {
        return instance;
    }
    
    public PlayerManager getPlayerManager() { return playerManager; }
    public SoulManager getSoulManager() { return soulManager; }
    public TaskManager getTaskManager() { return taskManager; }
    public ConfigManager getConfigManager() { return configManager; }
    public DatabaseManager getDatabaseManager() { return databaseManager; }
    public ScoreboardManager getScoreboardManager() { return scoreboardManager; }
    public ParticleManager getParticleManager() { return particleManager; }
}
