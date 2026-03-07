package com.soloarise.plugin.managers;

import com.soloarise.plugin.SoloArisePlugin;
import com.soloarise.plugin.models.CapturedSoul;
import org.bukkit.entity.Mob;
import org.bukkit.scheduler.BukkitRunnable;

public class HealthBarTask extends BukkitRunnable {
    
    private final SoloArisePlugin plugin;
    private final Mob mob;
    private final CapturedSoul soul;
    
    public HealthBarTask(SoloArisePlugin plugin, Mob mob, CapturedSoul soul) {
        this.plugin = plugin;
        this.mob = mob;
        this.soul = soul;
    }
    
    @Override
    public void run() {
        if (mob.isDead() || !mob.isValid()) {
            cancel();
            return;
        }
        
        double currentHealth = mob.getHealth();
        double maxHealth = mob.getMaxHealth();
        
        // Update soul health
        soul.setCurrentHealth(currentHealth);
        
        // Update health bar in name
        String healthBar = getHealthBar(currentHealth, maxHealth);
        mob.setCustomName(soul.getRank().getDisplayName() + " §f" + soul.getName() + " " + healthBar);
        
        // Check if dead
        if (currentHealth <= 0) {
            soul.setSummoned(false);
            soul.setSummonedEntity(null);
            cancel();
        }
    }
    
    private String getHealthBar(double current, double max) {
        int bars = 10;
        int healthBars = (int) ((current / max) * bars);
        
        StringBuilder bar = new StringBuilder("§c");
        for (int i = 0; i < healthBars; i++) bar.append("❤");
        bar.append("§8");
        for (int i = healthBars; i < bars; i++) bar.append("❤");
        
        return bar.toString();
    }
}
