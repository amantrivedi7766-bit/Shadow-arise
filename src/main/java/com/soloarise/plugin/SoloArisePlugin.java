package com.soloarise.plugin;

import com.soloarise.plugin.commands.*;
import com.soloarise.plugin.commands.admin.AdminPanelCommand;
import com.soloarise.plugin.listeners.*;
import com.soloarise.plugin.managers.*;
import org.bukkit.Bukkit;
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
        
        // Start schedulers
        startSchedulers();
        
        getLogger().info("§a✓ SoloArisePlugin has been enabled!");
        getLogger().info("§7Version: 1.0.0");
        getLogger().info("§7Author: YourName");
    }
    
    @Override
    public void onDisable() {
        // Save all player data
        if (databaseManager != null) {
            databaseManager.saveAll();
        }
        
        // Recall all summoned souls
        if (soulManager != null && playerManager != null) {
            Bukkit.getOnlinePlayers().forEach(player -> {
                soulManager.recallAllSouls(player);
            });
        }
        
        getLogger().info("§c✗ SoloArisePlugin has been disabled!");
    }
    
    private void registerCommands() {
        // Player commands
        getCommand("arise").setExecutor(new AriseCommand(this));
        getCommand("arisework").setExecutor(new AriseWorkCommand(this));
        getCommand("soulrelease").setExecutor(new SoulReleaseCommand(this));
        getCommand("soulattack").setExecutor(new SoulAttackCommand(this));
        getCommand("soulcome").setExecutor(new SoulComeCommand(this));
        getCommand("soultask").setExecutor(new SoulTaskCommand(this));
        getCommand("summon").setExecutor(new SummonCommand(this));
        getCommand("healsoul").setExecutor(new HealSoulCommand(this));
        
        // Admin commands
        getCommand("solarise").setExecutor(new AdminPanelCommand(this));
        
        // Command completers
        getCommand("soulrelease").setTabCompleter(new SoulTabCompleter(this));
        getCommand("soultask").setTabCompleter(new SoulTabCompleter(this));
        getCommand("summon").setTabCompleter(new SummonTabCompleter(this));
        getCommand("solarise").setTabCompleter(new AdminTabCompleter(this));
    }
    
    private void registerListeners() {
        // Player listeners
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerInteractListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerMoveListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerChatListener(this), this);
        
        // Entity listeners
        getServer().getPluginManager().registerEvents(new MobDeathListener(this), this);
        getServer().getPluginManager().registerEvents(new EntityDamageListener(this), this);
        getServer().getPluginManager().registerEvents(new EntityTargetListener(this), this);
        
        // World listeners
        getServer().getPluginManager().registerEvents(new WorldListener(this), this);
        
        // Inventory listeners
        getServer().getPluginManager().registerEvents(new InventoryListener(this), this);
    }
    
    private void startSchedulers() {
        // Auto-save every 5 minutes
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            databaseManager.saveAll();
            getLogger().info("Auto-saved player data");
        }, 6000L, 6000L); // 5 minutes = 6000 ticks
        
        // Update scoreboards every 2 seconds
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            scoreboardManager.updateAllScoreboards();
        }, 40L, 40L); // 2 seconds = 40 ticks
        
        // Clean up old data every 10 minutes
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            // Clean up summon sessions
            // This is handled in SummonSessionManager
        }, 12000L, 12000L); // 10 minutes = 12000 ticks
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
