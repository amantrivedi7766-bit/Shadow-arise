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
    
    // Hunger system tracking
    private final Map<UUID, Integer> hungerTimer = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> summonedCount = new ConcurrentHashMap<>();
    
    public SoulManager(SoloArisePlugin plugin) {
        this.plugin = plugin;
        startHungerDrainTask();
    }
    
    // ===== HUNGER SYSTEM =====
    
    /**
     * Start the hunger drain task that runs every 20 seconds
     * Each summoned soul makes hunger drain faster
     * Base: 1 hunger point per 20 seconds per soul
     */
    private void startHungerDrainTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    PlayerSoulData soulData = playerSouls.get(player.getUniqueId());
                    
                    if (soulData != null) {
                        int summonedSouls = soulData.getActiveSummonCount();
                        
                        if (summonedSouls > 0) {
                            // Calculate hunger drain: 1 point per soul every 20 seconds
                            // Each soul adds 1 point of hunger drain
                            int totalDrain = summonedSouls;
                            
                            // Apply hunger drain
                            int currentFood = player.getFoodLevel();
                            int newFood = Math.max(0, currentFood - totalDrain);
                            player.setFoodLevel(newFood);
                            
                            // Visual feedback if hunger is low
                            if (newFood <= 6) {
                                player.sendMessage("§c⚠ Your hunger is running low! Souls drain your hunger!");
                            }
                            
                            // Update scoreboard to show active souls
                            plugin.getScoreboardManager().updateMainScoreboard(player);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 400L, 400L); // Run every 20 seconds (400 ticks)
    }
    
    // ===== CAPTURE SYSTEM =====
    
    /**
     * Attempt to capture a soul from a killed mob
     */
    public boolean attemptCapture(Player player, LivingEntity target) {
        PlayerSoulData soulData = playerSouls.computeIfAbsent(player.getUniqueId(), 
            k -> new PlayerSoulData(player.getUniqueId()));
        
        String mobType = target.getType().name();
        
        // Check if already have this mob type
        if (soulData.hasMobType(mobType)) {
            player.sendMessage("§cYou already have a " + formatMobName(mobType) + " soul!");
            return false;
        }
        
        // Check capture attempts for this specific mob
        Map<String, Boolean> attempts = captureAttempts.computeIfAbsent(player.getUniqueId(), 
            k -> new ConcurrentHashMap<>());
        
        String mobId = target.getUniqueId().toString();
        if (attempts.containsKey(mobId)) {
            player.sendMessage("§cYou've already attempted to capture this specific mob!");
            return false;
        }
        
        // 50% chance
        boolean success = Math.random() < 0.5;
        
        // Mark this mob as attempted
        attempts.put(mobId, success);
        
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
            player.sendMessage("§7Rank: " + soul.getRank().getDisplayName());
            player.sendMessage("§7Ability: §f" + soul.getAbility().getName());
            
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
    
    /**
     * Spawn capture particles
     */
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
    
    /**
     * Open the summon menu for player
     */
    public void openSummonMenu(Player player) {
        PlayerSoulData soulData = playerSouls.get(player.getUniqueId());
        
        if (soulData == null || soulData.getSouls().isEmpty()) {
            player.sendMessage("§cYou have no souls to summon!");
            return;
        }
        
        // Open custom head menu
        plugin.getHeadManager().openSoulSelectionMenu(player, soulData);
    }
    
    /**
     * Summon a specific soul
     */
    public boolean summonSoul(Player player, CapturedSoul soul) {
        // Check if already summoned
        if (soul.isSummoned()) {
            player.sendMessage("§cThis soul is already summoned!");
            return false;
        }
        
        // Check if soul is dead
        if (soul.isDead()) {
            player.sendMessage("§cThis soul is dead! Use §e/soulheal " + soul.getFormattedName() + " §cto revive it.");
            return false;
        }
        
        // Check cooldown
        long lastSummon = lastSummonTime.getOrDefault(player.getUniqueId(), 0L);
        if (System.currentTimeMillis() - lastSummon < 1000) { // 1 second cooldown
            return false;
        }
        
        Location spawnLoc = player.getLocation().add(2, 0, 0);
        Entity entity = spawnSoulEntity(spawnLoc, soul, player);
        
        if (entity == null) return false;
        
        soul.setSummoned(true);
        soul.setSummonedEntity(entity);
        soul.setSummonLocation(spawnLoc);
        soul.setSummonTime(System.currentTimeMillis());
        
        lastSummonTime.put(player.getUniqueId(), System.currentTimeMillis());
        
        // Summon particles
        spawnSummonParticles(spawnLoc, soul);
        
        player.sendMessage("§a✦ Summoned: " + soul.getColoredName() + " §f" + 
            soul.getFormattedName());
        player.sendMessage("§7Ability: §f" + soul.getAbility().getName());
        player.sendMessage("§7Hunger will drain §c2x faster§7 while souls are active!");
        
        plugin.getScoreboardManager().updateMainScoreboard(player);
        
        return true;
    }
    
    /**
     * Spawn the soul entity
     */
    private Entity spawnSoulEntity(Location loc, CapturedSoul soul, Player owner) {
        try {
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
                new SoulAI(plugin, living, soul, owner).runTaskTimer(plugin, 0L, 10L);
            }
            
            return entity;
            
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Could not spawn entity type: " + soul.getMobType());
            return null;
        }
    }
    
    /**
     * Spawn summon particles
     */
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
                
                // Rank-based particles
                Particle rankParticle = switch(soul.getRank()) {
                    case NORMAL -> Particle.SPELL;
                    case WARRIOR -> Particle.CRIT;
                    case ELITE -> Particle.ENCHANT;
                    case BOSS -> Particle.DRAGON_BREATH;
                    case PLAYER -> Particle.HEART;
                };
                
                loc.getWorld().spawnParticle(
                    rankParticle,
                    loc.clone().add(0, 1, 0),
                    10, 0.5, 0.5, 0.5, 0.1
                );
                
                tick++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
        
        loc.getWorld().playSound(loc, Sound.ENTITY_ILLUSIONER_CAST_SPELL, 1.0f, 1.0f);
    }
    
    // ===== SOUL AI UPDATE =====
    
    /**
     * Update all summoned souls
     */
    public void updateAllSouls() {
        for (PlayerSoulData soulData : playerSouls.values()) {
            for (CapturedSoul soul : soulData.getSummonedSouls()) {
                Entity entity = soul.getSummonedEntity();
                
                if (entity == null || entity.isDead() || !entity.isValid()) {
                    soul.setSummoned(false);
                    soul.setSummonedEntity(null);
                    continue;
                }
                
                if (entity instanceof Mob mob) {
                    // Update health bar
                    mob.setCustomName(soul.getDisplayName());
                    soul.setCurrentHealth(mob.getHealth());
                    
                    // Check if soul died
                    if (mob.getHealth() <= 0) {
                        soul.setSummoned(false);
                        soul.setSummonedEntity(null);
                    }
                }
            }
        }
    }
    
    // ===== RECALL SYSTEM =====
    
    /**
     * Recall all souls for a player
     */
    public void recallSouls(Player player) {
        PlayerSoulData soulData = playerSouls.get(player.getUniqueId());
        if (soulData == null) return;
        
        int recalled = 0;
        for (CapturedSoul soul : soulData.getSummonedSouls()) {
            if (soul.getSummonedEntity() != null) {
                soul.getSummonedEntity().remove();
                recalled++;
            }
            soul.setSummoned(false);
            soul.setSummonedEntity(null);
        }
        
        if (recalled > 0) {
            player.sendMessage("§d✦ Recalled " + recalled + " soul" + (recalled > 1 ? "s" : "") + "!");
        } else {
            player.sendMessage("§cYou have no summoned souls!");
        }
        
        plugin.getScoreboardManager().updateMainScoreboard(player);
    }
    
    /**
     * Recall all souls for all players
     */
    public void recallAllSouls() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            recallSouls(player);
        }
    }
    
    // ===== HEALING SYSTEM =====
    
    /**
     * Heal a specific soul
     */
    public boolean healSoul(Player player, String soulName) {
        PlayerSoulData soulData = playerSouls.get(player.getUniqueId());
        if (soulData == null) return false;
        
        CapturedSoul soul = soulData.getSoulByName(soulName);
        if (soul == null) return false;
        
        soul.healFully();
        
        player.sendMessage("§a✓ " + soul.getFormattedName() + " has been fully healed!");
        plugin.getScoreboardManager().updateMainScoreboard(player);
        
        return true;
    }
    
    /**
     * Heal all souls for all players
     */
    public void healAllSouls() {
        for (PlayerSoulData soulData : playerSouls.values()) {
            for (CapturedSoul soul : soulData.getSouls()) {
                soul.setCurrentHealth(soul.getMaxHealth());
                
                // Update summoned entities
                if (soul.isSummoned() && soul.getSummonedEntity() != null) {
                    if (soul.getSummonedEntity() instanceof LivingEntity living) {
                        living.setHealth(soul.getMaxHealth());
                    }
                }
            }
        }
        
        Bukkit.broadcastMessage("§a§l✦ All souls have been healed by an admin!");
    }
    
    // ===== CLEAR SYSTEM =====
    
    /**
     * Clear all souls for all players
     */
    public void clearAllSouls() {
        // First recall all summoned souls
        recallAllSouls();
        
        // Clear all soul data
        playerSouls.clear();
        captureAttempts.clear();
        
        // Update all scoreboards
        for (Player player : Bukkit.getOnlinePlayers()) {
            plugin.getScoreboardManager().updateMainScoreboard(player);
        }
        
        Bukkit.broadcastMessage("§c§l✦ All souls have been cleared by an admin!");
    }
    
    // ===== UTILITY METHODS =====
    
    /**
     * Determine soul rank based on entity type
     */
    private SoulRank determineSoulRank(Entity entity) {
        if (entity instanceof Player) return SoulRank.PLAYER;
        
        if (entity instanceof EnderDragon) return SoulRank.BOSS;
        if (entity instanceof Wither) return SoulRank.BOSS;
        if (entity instanceof Warden) return SoulRank.BOSS;
        
        if (entity instanceof Ghast) return SoulRank.ELITE;
        if (entity instanceof Ravager) return SoulRank.ELITE;
        if (entity instanceof Evoker) return SoulRank.ELITE;
        if (entity instanceof Vindicator) return SoulRank.ELITE;
        if (entity instanceof Pillager) return SoulRank.ELITE;
        
        if (entity instanceof Monster) return SoulRank.WARRIOR;
        
        return SoulRank.NORMAL;
    }
    
    /**
     * Format mob name for display
     */
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
    
    /**
     * Get recent kill for chat capture
     */
    public void attemptChatCapture(Player player) {
        // This would need a kill queue - simplified version
        player.sendMessage("§cKill a mob first, then type /arise!");
    }
    
    // ===== GETTERS =====
    
    public Map<UUID, PlayerSoulData> getPlayerSouls() {
        return playerSouls;
    }
    
    public PlayerSoulData getPlayerSoulData(Player player) {
        return playerSouls.get(player.getUniqueId());
    }
            }
