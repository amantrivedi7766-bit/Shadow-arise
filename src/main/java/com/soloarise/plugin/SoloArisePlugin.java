package com.soloarise.plugin;

import com.soloarise.plugin.commands.*;
import com.soloarise.plugin.commands.admin.AdminCommand;
import com.soloarise.plugin.listeners.*;
import com.soloarise.plugin.managers.*;
import org.bukkit.plugin.java.JavaPlugin;

public class SoloArisePlugin extends JavaPlugin {
    
    private static SoloArisePlugin instance;
    
    // Managers
    private ConfigManager configManager;
    private SoulManager soulManager;
    private ScoreboardManager scoreboardManager;
    private ParticleManager particleManager;
    private AbilityManager abilityManager;
    private HeadManager headManager;
    private AdminMenuManager adminMenuManager;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // Initialize configurations
        saveDefaultConfig();
        configManager = new ConfigManager(this);
        
        // Initialize managers
        soulManager = new SoulManager(this);
        scoreboardManager = new ScoreboardManager(this);
        particleManager = new ParticleManager(this);
        abilityManager = new AbilityManager(this);
        headManager = new HeadManager(this);
        adminMenuManager = new AdminMenuManager(this);
        
        // Register commands
        registerCommands();
        
        // Register listeners
        registerListeners();
        
        // Start update schedulers
        startSchedulers();
        
        getLogger().info("§a✓ SoloArise Plugin Enabled!");
        getLogger().info("§7Version: 1.0.0");
    }
    
    @Override
    public void onDisable() {
        // Recall all summoned souls
        if (soulManager != null) {
            soulManager.recallAllSouls();
        }
        
        getLogger().info("§c✗ SoloArise Plugin Disabled!");
    }
    
    private void registerCommands() {
        // Player commands
        getCommand("arise").setExecutor(new AriseCommand(this));
        getCommand("soulcome").setExecutor(new SoulComeCommand(this));
        getCommand("soulrelease").setExecutor(new SoulReleaseCommand(this));
        getCommand("soulability").setExecutor(new SoulAbilityCommand(this));
        getCommand("soulheal").setExecutor(new SoulHealCommand(this));
        getCommand("souls").setExecutor(new SoulsCommand(this));
        
        // Admin command - FIXED: now using "soloarise" not "solarise"
        getCommand("soloarise").setExecutor(new AdminCommand(this));
    }
    
    private void registerListeners() {
        // Player listeners
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerCrouchListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerInteractListener(this), this);
        
        // Entity listeners
        getServer().getPluginManager().registerEvents(new MobDeathListener(this), this);
        getServer().getPluginManager().registerEvents(new EntityDamageListener(this), this);
        getServer().getPluginManager().registerEvents(new EntityTargetListener(this), this);
        
        // Menu listeners
        getServer().getPluginManager().registerEvents(new MenuClickListener(this), this);
        getServer().getPluginManager().registerEvents(new AdminMenuListener(this), this);
    }
    
    private void startSchedulers() {
        // Update scoreboards every 2 seconds
        getServer().getScheduler().runTaskTimer(this, () -> {
            if (scoreboardManager != null) {
                scoreboardManager.updateAllScoreboards();
            }
        }, 40L, 40L);
        
        // Update soul AI every second
        getServer().getScheduler().runTaskTimer(this, () -> {
            if (soulManager != null) {
                soulManager.updateAllSouls();
            }
        }, 20L, 20L);
    }
    
    // Getters
    public static SoloArisePlugin getInstance() { return instance; }
    public ConfigManager getConfigManager() { return configManager; }
    public SoulManager getSoulManager() { return soulManager; }
    public ScoreboardManager getScoreboardManager() { return scoreboardManager; }
    public ParticleManager getParticleManager() { return particleManager; }
    public AbilityManager getAbilityManager() { return abilityManager; }
    public HeadManager getHeadManager() { return headManager; }
    public AdminMenuManager getAdminMenuManager() { return adminMenuManager; }
}
