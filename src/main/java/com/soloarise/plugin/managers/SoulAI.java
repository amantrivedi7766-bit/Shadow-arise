package com.soloarise.plugin.managers;

import com.soloarise.plugin.SoloArisePlugin;
import com.soloarise.plugin.models.CapturedSoul;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class SoulAI extends BukkitRunnable {
    
    private final SoloArisePlugin plugin;
    private final Mob mob;
    private final CapturedSoul soul;
    private final World world;
    private Player owner;
    private Location lastOwnerLocation;
    private int stuckTicks = 0;
    
    public SoulAI(SoloArisePlugin plugin, Mob mob, CapturedSoul soul, World world) {
        this.plugin = plugin;
        this.mob = mob;
        this.soul = soul;
        this.world = world;
    }
    
    @Override
    public void run() {
        // Check if mob is dead
        if (mob.isDead() || !mob.isValid()) {
            soul.setSummoned(false);
            soul.setSummonedEntity(null);
            cancel();
            return;
        }
        
        // Find owner
        if (owner == null || !owner.isOnline()) {
            owner = world.getPlayers().stream()
                .filter(p -> p.getUniqueId().equals(soul.getOwnerId()))
                .findFirst()
                .orElse(null);
                
            if (owner == null) {
                // Owner offline, despawn
                mob.remove();
                soul.setSummoned(false);
                soul.setSummonedEntity(null);
                cancel();
                return;
            }
        }
        
        // Update health in soul object
        soul.setCurrentHealth(mob.getHealth());
        
        // Update display name with health bar
        mob.setCustomName(soul.getDisplayName());
        
        // Follow owner if too far
        double distanceToOwner = mob.getLocation().distance(owner.getLocation());
        
        if (distanceToOwner > 10) {
            // Teleport if too far
            mob.teleport(owner.getLocation().add(2, 0, 0));
            stuckTicks = 0;
        } else if (distanceToOwner > 3) {
            // Walk towards owner
            mob.getPathfinder().moveTo(owner.getLocation(), 1.0);
        }
        
        // Check if stuck
        if (lastOwnerLocation != null && 
            lastOwnerLocation.distance(owner.getLocation()) < 1) {
            stuckTicks++;
            
            if (stuckTicks > 40) { // Stuck for 4 seconds
                mob.teleport(owner.getLocation().add(2, 0, 0));
                stuckTicks = 0;
            }
        } else {
            stuckTicks = 0;
        }
        
        lastOwnerLocation = owner.getLocation().clone();
        
        // 5x5 aggro range for enemies targeting owner
        for (Entity nearby : mob.getNearbyEntities(5, 5, 5)) {
            if (nearby instanceof Monster monster && !monster.isDead()) {
                if (monster.getTarget() == owner) {
                    mob.setTarget(monster);
                    
                    // Use special ability
                    plugin.getAbilityManager().useAbility(mob, soul, monster);
                    break;
                }
            }
        }
    }
}
