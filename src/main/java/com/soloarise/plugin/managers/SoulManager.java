package com.soloarise.plugin.managers;

import com.soloarise.plugin.SoloArisePlugin;
import com.soloarise.plugin.models.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SoulManager {
    
    private final SoloArisePlugin plugin;
    private final Map<UUID, PlayerSoulData> playerSouls = new ConcurrentHashMap<>();
    private final Map<UUID, Map<String, Boolean>> captureAttempts = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastSummonTime = new ConcurrentHashMap<>();
    
    public SoulManager(SoloArisePlugin plugin) {
        this.plugin = plugin;
    }
    
    // ===== CAPTURE SYSTEM =====
    public boolean attemptCapture(Player player, LivingEntity target) {
        PlayerSoulData soulData = playerSouls.computeIfAbsent(player.getUniqueId(), 
            k -> new PlayerSoulData(player.getUniqueId()));
        
        String mobType = target.getType().name();
        
        // Check if already have this mob type
        if (soulData.hasMobType(mobType)) {
            player.sendMessage("§cYou already have a " + formatMobName(mobType) + " soul!");
            return false;
        }
        
        // Check capture attempts
        Map<String, Boolean> attempts = captureAttempts.computeIfAbsent(player.getUniqueId(), 
            k -> new ConcurrentHashMap<>());
        
        if (attempts.containsKey(target.getUniqueId().toString())) {
            player.sendMessage("§cYou've already attempted to capture this specific mob!");
            return false;
        }
        
        // 50% chance
        boolean success = Math.random() < 0.5;
        
        // Mark this mob as attempted
        attempts.put(target.getUniqueId().toString(), success);
        
        if (success) {
            // Create new soul
            CapturedSoul soul = new CapturedSoul(
                UUID.randomUUID(),
                mobType,
                target.getName(),
                determineSoulRank(target),
                target.getLocation()
            );
            
            soulData.addSoul(soul);
            
            // Epic capture particles
            spawnCaptureParticles(target.getLocation(), true);
            
            player.sendMessage("§a§l✦ SOUL CAPTURED! ✦");
            player.sendMessage("§7You now have a §f" + formatMobName(mobType) + "§7 soul!");
            player.sendMessage("§7Type: §f" + soul.getRank().getDisplayName());
            
            // Remove the mob
            target.remove();
            
        } else {
            // Failed capture - permanent loss
            spawnCaptureParticles(target.getLocation(), false);
            
            player.sendMessage("§c§l❌ CAPTURE FAILED! ❌");
            player.sendMessage("§7The soul has been lost forever!");
            
            // Mob still dies but no soul
            target.setHealth(0);
        }
        
        // Update scoreboard
        plugin.getScoreboardManager().updateMainScoreboard(player);
        
        return success;
    }
    
    private void spawnCaptureParticles(Location loc, boolean success) {
        if (success) {
            // Success particles - epic golden spiral
            new BukkitRunnable() {
                int tick = 0;
                
                @Override
                public void run() {
                    if (tick >= 40) {
                        cancel();
                        return;
                    }
                    
                    double radius = 2.0;
                    double y = tick * 0.1;
                    
                    for (int i = 0; i < 4; i++) {
                        double angle = (tick * 0.3) + (i * Math.PI / 2);
                        double x = Math.cos(angle) * radius;
                        double z = Math.sin(angle) * radius;
                        
                        loc.getWorld().spawnParticle(
                            Particle.SOUL_FIRE_FLAME,
                            loc.clone().add(x, y, z),
                            1, 0, 0, 0, 0
                        );
                        
                        loc.getWorld().spawnParticle(
                            Particle.GLOW,
                            loc.clone().add(x / 2, y + 1, z / 2),
                            2, 0, 0, 0, 0.1
                        );
                    }
                    
                    loc.getWorld().spawnParticle(
                        Particle.FIREWORK,
                        loc.clone().add(0, 1, 0),
                        10, 0.5, 0.5, 0.5, 0.1
                    );
                    
                    tick++;
                }
            }.runTaskTimer(plugin, 0L, 1L);
            
            // Sound effects
            loc.getWorld().playSound(loc, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1.0f, 1.0f);
            loc.getWorld().playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 2.0f);
            
        } else {
            // Failure particles - dark smoke
            for (int i = 0; i < 3; i++) {
                final int delay = i * 5;
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        loc.getWorld().spawnParticle(
                            Particle.LARGE_SMOKE,
                            loc.clone().add(0, 1, 0),
                            30, 1, 1, 1, 0.2
                        );
                        
                        loc.getWorld().spawnParticle(
                            Particle.SOUL,
                            loc.clone().add(0, 1, 0),
                            20, 0.5, 0.5, 0.5, 0.1
                        );
                    }
                }.runTaskLater(plugin, delay);
            }
            
            loc.getWorld().playSound(loc, Sound.ENTITY_WITHER_HURT, 1.0f, 0.5f);
        }
    }
    
    // ===== SUMMON SYSTEM =====
    public void openSummonMenu(Player player) {
        PlayerSoulData soulData = playerSouls.get(player.getUniqueId());
        
        if (soulData == null || soulData.getSouls().isEmpty()) {
            player.sendMessage("§cYou have no souls to summon!");
            return;
        }
        
        // Open custom head menu
        plugin.getHeadManager().openSoulSelectionMenu(player, soulData);
    }
    
    public boolean summonSoul(Player player, CapturedSoul soul) {
        // Check if already summoned
        if (soul.isSummoned()) {
            player.sendMessage("§cThis soul is already summoned!");
            return false;
        }
        
        // Check cooldown
        long lastSummon = lastSummonTime.getOrDefault(player.getUniqueId(), 0L);
        if (System.currentTimeMillis() - lastSummon < 1000) { // 1 second cooldown
            return false;
        }
        
        Location spawnLoc = player.getLocation().add(2, 0, 0);
        Entity entity = spawnSoulEntity(spawnLoc, soul);
        
        if (entity == null) return false;
        
        soul.setSummoned(true);
        soul.setSummonedEntity(entity);
        soul.setSummonLocation(spawnLoc);
        soul.setSummonTime(System.currentTimeMillis());
        
        lastSummonTime.put(player.getUniqueId(), System.currentTimeMillis());
        
        // Summon particles
        spawnSummonParticles(spawnLoc, soul);
        
        player.sendMessage("§a✦ Summoned: " + soul.getRank().getDisplayName() + " §f" + 
            formatMobName(soul.getMobType()));
        
        plugin.getScoreboardManager().updateMainScoreboard(player);
        
        return true;
    }
    
    private Entity spawnSoulEntity(Location loc, CapturedSoul soul) {
        EntityType type = EntityType.valueOf(soul.getMobType());
        Entity entity = loc.getWorld().spawnEntity(loc, type);
        
        if (entity instanceof LivingEntity living) {
            // Set health
            living.setMaxHealth(soul.getMaxHealth());
            living.setHealth(soul.getCurrentHealth());
            
            // Set custom name with health bar
            living.setCustomName(soul.getDisplayName());
            living.setCustomNameVisible(true);
            
            // Make it follow owner and attack enemies
            new SoulAI(plugin, living, soul, loc.getWorld()).runTaskTimer(plugin, 0L, 10L);
        }
        
        return entity;
    }
    
    private void spawnSummonParticles(Location loc, CapturedSoul soul) {
        // Spiral summon effect
        new BukkitRunnable() {
            int tick = 0;
            
            @Override
            public void run() {
                if (tick >= 30) {
                    cancel();
                    return;
                }
                
                double radius = 1.5;
                double angle = tick * 0.4;
                
                for (int i = 0; i < 3; i++) {
                    double y = tick * 0.1;
                    double x = Math.cos(angle + i) * radius;
                    double z = Math.sin(angle + i) * radius;
                    
                    loc.getWorld().spawnParticle(
                        Particle.PORTAL,
                        loc.clone().add(x, y, z),
                        5, 0, 0, 0, 0.5
                    );
                }
                
                tick++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
        
        loc.getWorld().playSound(loc, Sound.ENTITY_ILLUSIONER_CAST_SPELL, 1.0f, 1.0f);
    }
    
    // ===== SOUL AI =====
    public void updateAllSouls() {
        for (PlayerSoulData soulData : playerSouls.values()) {
            for (CapturedSoul soul : soulData.getSummonedSouls()) {
                Entity entity = soul.getSummonedEntity();
                if (entity == null || entity.isDead()) {
                    soul.setSummoned(false);
                    soul.setSummonedEntity(null);
                    continue;
                }
                
                if (entity instanceof Mob mob) {
                    // Update health bar
                    mob.setCustomName(soul.getDisplayName());
                    soul.setCurrentHealth(mob.getHealth());
                    
                    // Check for enemies within 5x5 area
                    Player owner = Bukkit.getPlayer(soulData.getPlayerId());
                    if (owner != null) {
                        checkForEnemies(mob, owner, soul);
                    }
                }
            }
        }
    }
    
    private void checkForEnemies(Mob mob, Player owner, CapturedSoul soul) {
        // Don't attack owner
        if (mob.getTarget() == owner) {
            mob.setTarget(null);
            return;
        }
        
        // Look for enemies within 5 blocks
        for (Entity nearby : mob.getNearbyEntities(5, 5, 5)) {
            if (nearby instanceof Monster monster && !monster.isDead()) {
                // Check if monster is targeting owner
                if (monster.getTarget() == owner) {
                    mob.setTarget(monster);
                    
                    // Use special ability
                    plugin.getAbilityManager().useAbility(mob, soul, monster);
                    return;
                }
            }
            
            // Attack other players' souls? (Optional PvP)
            if (nearby instanceof Mob otherMob && otherMob != mob) {
                // Check if it's another player's soul
                // Implementation depends on your PvP preferences
            }
        }
    }
    
    // ===== UTILITY METHODS =====
    private SoulRank determineSoulRank(Entity entity) {
        if (entity instanceof Player) return SoulRank.PLAYER;
        if (entity instanceof EnderDragon) return SoulRank.BOSS;
        if (entity instanceof Wither) return SoulRank.BOSS;
        if (entity instanceof Warden) return SoulRank.BOSS;
        if (entity instanceof Ghast) return SoulRank.ELITE;
        if (entity instanceof WitherSkeleton) return SoulRank.ELITE;
        if (entity instanceof Monster) return SoulRank.WARRIOR;
        return SoulRank.NORMAL;
    }
    
    private String formatMobName(String mobType) {
        String[] words = mobType.toLowerCase().split("_");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (word.length() > 0) {
                result.append(Character.toUpperCase(word.charAt(0)))
                      .append(word.substring(1)).append(" ");
            }
        }
        return result.toString().trim();
    }
    
    public void recallSouls(Player player) {
        PlayerSoulData soulData = playerSouls.get(player.getUniqueId());
        if (soulData == null) return;
        
        for (CapturedSoul soul : soulData.getSummonedSouls()) {
            if (soul.getSummonedEntity() != null) {
                soul.getSummonedEntity().remove();
            }
            soul.setSummoned(false);
            soul.setSummonedEntity(null);
        }
        
        player.sendMessage("§d✦ All souls recalled!");
    }
    
    public void recallAllSouls() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            recallSouls(player);
        }
    }
    
    // Getters
    public Map<UUID, PlayerSoulData> getPlayerSouls() { return playerSouls; }
    public PlayerSoulData getPlayerSoulData(Player player) { 
        return playerSouls.get(player.getUniqueId()); 
    }
            }
