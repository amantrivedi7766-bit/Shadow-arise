package com.soloarise.plugin.managers;

import com.soloarise.plugin.SoloArisePlugin;
import com.soloarise.plugin.models.Ability;
import com.soloarise.plugin.models.CapturedSoul;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AbilityManager {
    
    private final SoloArisePlugin plugin;
    private final Map<String, Ability> abilities = new HashMap<>();
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    
    public AbilityManager(SoloArisePlugin plugin) {
        this.plugin = plugin;
        registerAbilities();
    }
    
    private void registerAbilities() {
        // Zombie - Infection (poison on hit)
        abilities.put("zombie", new Ability(
            "Infection",
            "§7Poisons enemies on hit",
            5000 // 5 second cooldown
        ));
        
        // Skeleton - Volley (shoots 3 arrows rapidly)
        abilities.put("skeleton", new Ability(
            "Volley",
            "§7Shoots 3 arrows rapidly",
            3000
        ));
        
        // Spider - Web Trap (slows enemies)
        abilities.put("spider", new Ability(
            "Web Trap",
            "§7Slows enemies with webs",
            4000
        ));
        
        // Creeper - Explosive Charge (damage area)
        abilities.put("creeper", new Ability(
            "Explosive Charge",
            "§7Explodes when near multiple enemies",
            10000
        ));
        
        // Enderman - Teleport Strike (teleports behind enemy)
        abilities.put("enderman", new Ability(
            "Teleport Strike",
            "§7Teleports behind enemies",
            3000
        ));
        
        // Blaze - Fire Storm (shoots fireballs)
        abilities.put("blaze", new Ability(
            "Fire Storm",
            "§7Shoots fireballs at enemies",
            5000
        ));
        
        // Wither Skeleton - Wither Strike (wither effect)
        abilities.put("wither_skeleton", new Ability(
            "Wither Strike",
            "§7Applies wither effect",
            6000
        ));
        
        // Wolf - Pack Leader (summons temporary wolves)
        abilities.put("wolf", new Ability(
            "Pack Leader",
            "§7Summons temporary wolves",
            15000
        ));
        
        // Iron Golem - Shield Bash (knocks back enemies)
        abilities.put("iron_golem", new Ability(
            "Shield Bash",
            "§7Knocks back and stuns enemies",
            8000
        ));
        
        // Ender Dragon - Dragon Breath (area damage)
        abilities.put("ender_dragon", new Ability(
            "Dragon Breath",
            "§7Breathes fire in an area",
            20000
        ));
        
        // Warden - Sonic Boom (long range attack)
        abilities.put("warden", new Ability(
            "Sonic Boom",
            "§7Long range sonic attack",
            15000
        ));
        
        // Ghast - Fireball Barrage (multiple fireballs)
        abilities.put("ghast", new Ability(
            "Fireball Barrage",
            "§7Shoots multiple fireballs",
            12000
        ));
        
        // Silverfish - Swarm (summons silverfish)
        abilities.put("silverfish", new Ability(
            "Swarm",
            "§7Summons silverfish allies",
            8000
        ));
        
        // Bee - Sting (poison + damage)
        abilities.put("bee", new Ability(
            "Sting",
            "§7Poisons and damages enemies",
            3000
        ));
        
        // Dolphin - Speed Boost (gives speed to owner)
        abilities.put("dolphin", new Ability(
            "Speed Boost",
            "§7Gives speed to owner",
            10000
        ));
        
        // Horse - Charge (dash attack)
        abilities.put("horse", new Ability(
            "Charge",
            "§7Dashes forward damaging enemies",
            5000
        ));
        
        // Llama - Spit (ranged attack)
        abilities.put("llama", new Ability(
            "Spit",
            "§7Spits at enemies from range",
            2000
        ));
        
        // Parrot - Distraction (distracts enemies)
        abilities.put("parrot", new Ability(
            "Distraction",
            "§7Distracts enemies with sound",
            4000
        ));
        
        // Fox - Steal (steals items from enemies)
        abilities.put("fox", new Ability(
            "Steal",
            "§7Steals items from enemies",
            6000
        ));
        
        // Goat - Ram (knocks back enemies)
        abilities.put("goat", new Ability(
            "Ram",
            "§7Knocks back enemies with ram",
            4000
        ));
        
        // Frog - Leap (jumps on enemies)
        abilities.put("frog", new Ability(
            "Leap",
            "§7Leaps onto enemies",
            3000
        ));
        
        // Allay - Collect (collects drops)
        abilities.put("allay", new Ability(
            "Collect",
            "§7Collects item drops for owner",
            5000
        ));
        
        // Axolotl - Regeneration (heals owner)
        abilities.put("axolotl", new Ability(
            "Regeneration",
            "§7Heals owner over time",
            10000
        ));
        
        // 50% of mobs have abilities, rest have default attacks
    }
    
    public void useAbility(Mob mob, CapturedSoul soul, LivingEntity target) {
        String mobType = soul.getMobType().toLowerCase();
        Ability ability = abilities.get(mobType);
        
        if (ability == null) return; // No special ability
        
        // Check cooldown
        if (cooldowns.containsKey(mob.getUniqueId())) {
            long lastUse = cooldowns.get(mob.getUniqueId());
            if (System.currentTimeMillis() - lastUse < ability.getCooldown()) {
                return; // Still on cooldown
            }
        }
        
        // Use ability based on mob type
        switch(mobType) {
            case "zombie":
                target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 1));
                mob.getWorld().spawnParticle(Particle.SPELL_MOB, target.getLocation(), 20, 0.5, 0.5, 0.5, 1);
                break;
                
            case "skeleton":
                // Shoot 3 arrows
                for (int i = 0; i < 3; i++) {
                    mob.launchProjectile(org.bukkit.entity.Arrow.class);
                }
                break;
                
            case "creeper":
                // Small explosion
                mob.getWorld().createExplosion(mob.getLocation(), 2, false, false);
                break;
                
            case "enderman":
                // Teleport behind target
                Location behind = target.getLocation().add(target.getLocation().getDirection().multiply(-2));
                mob.teleport(behind);
                mob.setTarget(target);
                break;
                
            case "blaze":
                // Shoot fireball
                mob.launchProjectile(org.bukkit.entity.Fireball.class);
                break;
                
            case "wither_skeleton":
                target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 100, 1));
                break;
                
            case "warden":
                // Sonic boom effect
                target.damage(10, mob);
                target.getWorld().spawnParticle(Particle.SONIC_BOOM, target.getLocation(), 1);
                break;
                
            // Add more abilities for other mobs
        }
        
        // Set cooldown
        cooldowns.put(mob.getUniqueId(), System.currentTimeMillis());
        
        // Visual effect
        mob.getWorld().spawnParticle(Particle.ENCHANT, mob.getLocation(), 20, 0.5, 0.5, 0.5, 1);
    }
    
    public Ability getAbility(String mobType) {
        return abilities.get(mobType.toLowerCase());
    }
}
