package com.soloarise.plugin.managers;

import com.soloarise.plugin.SoloArisePlugin;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class ParticleManager {
    
    private final SoloArisePlugin plugin;
    
    public ParticleManager(SoloArisePlugin plugin) {
        this.plugin = plugin;
    }
    
    public void spawnCaptureParticles(Location location) {
        if (!plugin.getConfig().getBoolean("particles.capture.enabled", true)) return;
        
        location.getWorld().spawnParticle(Particle.PORTAL, location, 100, 1, 1, 1, 0.5);
        location.getWorld().spawnParticle(Particle.SOUL, location, 50, 0.5, 1, 0.5, 0.1);
        location.getWorld().playSound(location, Sound.ENTITY_WITHER_SPAWN, 1.0f, 1.0f);
    }
    
    public void spawnRecallParticles(Player player) {
        if (!plugin.getConfig().getBoolean("particles.recall.enabled", true)) return;
        
        Location loc = player.getLocation();
        
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (ticks >= 40) {
                    cancel();
                    return;
                }
                
                double radius = 2.0;
                double y = ticks * 0.1;
                
                for (int i = 0; i < 8; i++) {
                    double angle = (ticks * 0.5 + i * 45) * Math.PI / 180;
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;
                    
                    loc.clone().add(x, y, z).getWorld().spawnParticle(
                        Particle.SOUL_FIRE_FLAME,
                        loc.clone().add(x, y, z),
                        1, 0, 0, 0, 0
                    );
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
        
        player.playSound(loc, Sound.ITEM_CHORUS_FRUIT_TELEPORT, 1.0f, 1.0f);
    }
    
    public void spawnSoulWorkParticles(Location location, String workType) {
        switch(workType.toLowerCase()) {
            case "mine":
                location.getWorld().spawnParticle(Particle.CRIT, location, 30, 1, 1, 1, 0.5);
                location.getWorld().playSound(location, Sound.BLOCK_STONE_BREAK, 0.5f, 1.0f);
                break;
            case "build":
                location.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, location, 30, 1, 1, 1);
                location.getWorld().playSound(location, Sound.BLOCK_WOOD_PLACE, 0.5f, 1.0f);
                break;
            case "farm":
                location.getWorld().spawnParticle(Particle.COMPOSTER, location, 30, 1, 1, 1);
                location.getWorld().playSound(location, Sound.ITEM_BONE_MEAL_USE, 0.5f, 1.0f);
                break;
            default:
                location.getWorld().spawnParticle(Particle.ENCHANT, location, 30, 1, 1, 1);
                break;
        }
    }
}
