package com.soloarise.plugin.managers;

import com.soloarise.plugin.SoloArisePlugin;
import com.soloarise.plugin.models.Ability;  // Fixed import
import com.soloarise.plugin.models.CapturedSoul;
import org.bukkit.Location;
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
        // Hostile mobs
        abilities.put("zombie", new Ability(
            "Infection",
            "Poisons enemies on hit",
            5000
        ));
        
        abilities.put("skeleton", new Ability(
            "Volley",
            "Shoots 3 arrows rapidly",
            3000
        ));
        
        abilities.put("spider", new Ability(
            "Web Trap",
            "Slows enemies with webs",
            4000
        ));
        
        abilities.put("creeper", new Ability(
            "Explosive Charge",
            "Explodes when near multiple enemies",
            10000
        ));
        
        abilities.put("enderman", new Ability(
            "Teleport Strike",
            "Teleports behind enemies",
            3000
        ));
        
        abilities.put("blaze", new Ability(
            "Fire Storm",
            "Shoots fireballs at enemies",
            5000
        ));
        
        abilities.put("wither_skeleton", new Ability(
            "Wither Strike",
            "Applies wither effect",
            6000
        ));
        
        abilities.put("ghast", new Ability(
            "Fireball Barrage",
            "Shoots multiple fireballs",
            12000
        ));
        
        abilities.put("magma_cube", new Ability(
            "Lava Splash",
            "Splashes lava on enemies",
            8000
        ));
        
        abilities.put("slime", new Ability(
            "Split",
            "Splits into smaller slimes",
            7000
        ));
        
        abilities.put("witch", new Ability(
            "Potion Throw",
            "Throws harmful potions",
            4000
        ));
        
        abilities.put("pillager", new Ability(
            "Crossbarrage",
            "Shoots rapid crossbow shots",
            3000
        ));
        
        abilities.put("vindicator", new Ability(
            "Chop",
            "Powerful axe attack",
            5000
        ));
        
        abilities.put("evoker", new Ability(
            "Fang Attack",
            "Summons fangs from ground",
            8000
        ));
        
        abilities.put("ravager", new Ability(
            "Stomp",
            "Stomps the ground damaging area",
            10000
        ));
        
        abilities.put("warden", new Ability(
            "Sonic Boom",
            "Long range sonic attack",
            15000
        ));
        
        abilities.put("ender_dragon", new Ability(
            "Dragon Breath",
            "Breathes fire in an area",
            20000
        ));
        
        abilities.put("wither", new Ability(
            "Wither Skulls",
            "Shoots explosive skulls",
            15000
        ));
        
        // Passive/Neutral mobs
        abilities.put("wolf", new Ability(
            "Pack Leader",
            "Summons temporary wolves",
            15000
        ));
        
        abilities.put("iron_golem", new Ability(
            "Shield Bash",
            "Knocks back and stuns enemies",
            8000
        ));
        
        abilities.put("snowman", new Ability(
            "Snowball Barrage",
            "Throws snowballs rapidly",
            2000
        ));
        
        abilities.put("bee", new Ability(
            "Sting",
            "Poisons and damages enemies",
            3000
        ));
        
        abilities.put("dolphin", new Ability(
            "Speed Boost",
            "Gives speed to owner",
            10000
        ));
        
        abilities.put("horse", new Ability(
            "Charge",
            "Dashes forward damaging enemies",
            5000
        ));
        
        abilities.put("llama", new Ability(
            "Spit",
            "Spits at enemies from range",
            2000
        ));
        
        abilities.put("parrot", new Ability(
            "Distraction",
            "Distracts enemies with sound",
            4000
        ));
        
        abilities.put("fox", new Ability(
            "Steal",
            "Steals items from enemies",
            6000
        ));
        
        abilities.put("goat", new Ability(
            "Ram",
            "Knocks back enemies with ram",
            4000
        ));
        
        abilities.put("frog", new Ability(
            "Leap",
            "Leaps onto enemies",
            3000
        ));
        
        abilities.put("allay", new Ability(
            "Collect",
            "Collects item drops for owner",
            5000
        ));
        
        abilities.put("axolotl", new Ability(
            "Regeneration",
            "Heals owner over time",
            10000
        ));
        
        abilities.put("turtle", new Ability(
            "Shell Defense",
            "Reduces damage taken",
            8000
        ));
        
        abilities.put("panda", new Ability(
            "Sneeze",
            "Sneezes knocking back enemies",
            5000
        ));
        
        abilities.put("polar_bear", new Ability(
            "Slash",
            "Powerful claw attack",
            6000
        ));
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
                
            case "spider":
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 2));
                mob.getWorld().spawnParticle(Particle.BLOCK_CRACK, target.getLocation(), 30, 0.5, 0.5, 0.5, 0.1);
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
                mob.getWorld().spawnParticle(Particle.PORTAL, behind, 30, 0.5, 0.5, 0.5, 0.1);
                break;
                
            case "blaze":
                // Shoot fireball
                mob.launchProjectile(org.bukkit.entity.Fireball.class);
                break;
                
            case "wither_skeleton":
                target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 100, 1));
                mob.getWorld().spawnParticle(Particle.SOUL, target.getLocation(), 20, 0.5, 0.5, 0.5, 0.1);
                break;
                
            case "warden":
                // Sonic boom effect
                target.damage(10, mob);
                mob.getWorld().spawnParticle(Particle.SONIC_BOOM, target.getLocation(), 1);
                break;
                
            case "wolf":
                // Summon temporary wolf allies
                for (int i = 0; i < 2; i++) {
                    Location spawnLoc = mob.getLocation().add(2, 0, 0);
                    mob.getWorld().spawnEntity(spawnLoc, org.bukkit.entity.EntityType.WOLF);
                }
                break;
                
            case "bee":
                target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 60, 0));
                target.damage(2, mob);
                break;
                
            case "dolphin":
                if (target instanceof Player player) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 1));
                }
                break;
        }
        
        // Set cooldown
        cooldowns.put(mob.getUniqueId(), System.currentTimeMillis());
        
        // Visual effect
        mob.getWorld().spawnParticle(Particle.ENCHANT, mob.getLocation(), 20, 0.5, 0.5, 0.5, 1);
    }
    
    public Ability getAbility(String mobType) {
        return abilities.get(mobType.toLowerCase());
    }
    
    public boolean hasAbility(String mobType) {
        return abilities.containsKey(mobType.toLowerCase());
    }
        }
