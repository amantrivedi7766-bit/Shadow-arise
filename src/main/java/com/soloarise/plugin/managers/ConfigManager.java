package com.soloarise.plugin.managers;

import com.soloarise.plugin.SoloArisePlugin;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {
    
    private final SoloArisePlugin plugin;
    private FileConfiguration config;
    
    public ConfigManager(SoloArisePlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        loadConfig();
    }
    
    private void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }
    
    public double getCaptureChance() {
        return config.getDouble("soul-capture.base-chance", 0.7);
    }
    
    public double getBossCaptureChance() {
        return config.getDouble("soul-capture.boss-capture-chance", 0.3);
    }
    
    public int getMaxSoulsPerPlayer() {
        return config.getInt("soul-capture.max-souls-per-player", 10000);
    }
    
    public int getTaskTimeLimit() {
        return config.getInt("tasks.time-limit", 3600);
    }
    
    public int getMaxAttempts() {
        return config.getInt("tasks.max-attempts", 3);
    }
    
    public int getWorkEnergyCost() {
        return config.getInt("soul-energy.work-cost", 10);
    }
    
    public int getAttackEnergyCost() {
        return config.getInt("soul-energy.attack-cost", 5);
    }
    
    public int getHealAmountPerDiamond() {
        return config.getInt("soul-energy.heal-amount-per-diamond", 10);
    }
    
    public void reload() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }
}
