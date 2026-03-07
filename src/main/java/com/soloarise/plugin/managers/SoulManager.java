package com.soloarise.plugin.managers;

import com.soloarise.plugin.SoloArisePlugin;
import com.soloarise.plugin.models.*;
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
    private final Map<UUID, Map<UUID, Integer>> captureAttempts = new ConcurrentHashMap<>();
    private final Map<UUID, List<LivingEntity>> killQueue = new ConcurrentHashMap<>();
    private final Map<UUID, Map<UUID, Boolean>> permanentFailures = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastSummonTime = new ConcurrentHashMap<>();
    private final Map<UUID, Map<UUID, Long>> soulCooldowns = new ConcurrentHashMap<>();
    
    public SoulManager(SoloArisePlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Attempt to capture a soul from a killed entity
     */
    public boolean attemptCapture(Player player, Entity target) {
        PlayerSoulData soulData = playerSouls.computeIfAbsent(player.getUniqueId(), k -> new PlayerSoulData(player.getUniqueId()));
        
        // Check if player has arise power
        if (!plugin.getPlayerManager().getPlayer(player).hasArisePower()) {
            player.sendMessage("§cYou haven't unlocked the Arise power yet!");
            return false;
        }
        
        // Check if already captured
        if (soulData.hasSoul(target.getUniqueId())) {
            player.sendMessage("§cYou have already captured this soul!");
            return false;
        }
        
        // Check permanent failure
        Map<UUID, Boolean> failures = permanentFailures.computeIfAbsent(player.getUniqueId(), k -> new ConcurrentHashMap<>());
        if (failures.getOrDefault(target.getUniqueId(), false)) {
            player.sendMessage("§cYou have permanently failed to capture this soul!");
            return false;
        }
        
        // Check capture attempts
        Map<UUID, Integer> attempts = captureAttempts.computeIfAbsent(player.getUniqueId(), k -> new ConcurrentHashMap<>());
        int currentAttempts = attempts.getOrDefault(target.getUniqueId(), 0);
        
        if (currentAttempts >= 1) { // Only 1 attempt per mob
            player.sendMessage("§cYou can only attempt to capture a soul once!");
            return false;
        }
        
        // 50% success, 50% fail chance
        boolean success = Math.random() < 0.5;
        
        if (success) {
            // Successful capture
            SoulRank rank = determineSoulRank(target);
            CapturedSoul soul = new CapturedSoul(target.getUniqueId(), target.getName(), rank);
            
            // Set additional properties based on entity type
            if (target instanceof Mob mob) {
                soul.setMaxHealth(mob.getMaxHealth());
                soul.setCurrentHealth(mob.getMaxHealth());
                soul.setEntityType(mob.getType());
            } else if (target instanceof Player) {
                soul.setMaxHealth(40.0); // Players have 20 hearts = 40 health
                soul.setCurrentHealth(40.0);
                soul.setEntityType(EntityType.PLAYER);
            }
            
            soulData.addSoul(soul);
            
            // Reset attempts on success
            attempts.remove(target.getUniqueId());
            
            // Spawn particles
            spawnCaptureParticles(target.getLocation());
            
            player.sendMessage("§a✓ Successfully captured " + target.getName() + "'s soul!");
            player.sendMessage("§7Rank: " + rank.getDisplayName());
            
            // Update scoreboard
            plugin.getScoreboardManager().updateMainScoreboard(player);
            
            // Remove the entity if it's a mob
            if (target instanceof Mob) {
                target.remove();
            }
            
            return true;
        } else {
            // Failed capture - permanent failure
            failures.put(target.getUniqueId(), true);
            attempts.put(target.getUniqueId(), currentAttempts + 1);
            
            // Spawn failure particles
            spawnFailureParticles(target.getLocation());
            
            player.sendMessage("§c❌ Failed to capture soul! This soul is lost forever!");
            
            return false;
        }
    }
    
    /**
     * Summon a soul
     */
    public boolean summonSoul(Player player, UUID soulId) {
        PlayerSoulData soulData = playerSouls.get(player.getUniqueId());
        if (soulData == null) {
            player.sendMessage("§cYou have no souls!");
            return false;
        }
        
        CapturedSoul soul = soulData.getSoulById(soulId);
        if (soul == null) {
            player.sendMessage("§cSoul not found!");
            return false;
        }
        
        // Check cooldown
        Map<UUID, Long> cooldowns = soulCooldowns.computeIfAbsent(player.getUniqueId(), k -> new ConcurrentHashMap<>());
        long lastSummon = cooldowns.getOrDefault(soulId, 0L);
        long cooldownTime = getSoulCooldown(soul);
        
        if (System.currentTimeMillis() - lastSummon < cooldownTime) {
            long remaining = (cooldownTime - (System.currentTimeMillis() - lastSummon)) / 1000;
            player.sendMessage("§cSoul is on cooldown! §e" + remaining + "s remaining");
            return false;
        }
        
        // Check if soul is already summoned
        if (soul.isSummoned()) {
            player.sendMessage("§cThis soul is already summoned!");
            return false;
        }
        
        // Check if soul has health
        if (soul.getCurrentHealth() <= 0) {
            player.sendMessage("§cThis soul has no health left! It needs to be healed.");
            return false;
        }
        
        // Check max summoned souls limit
        long summonedCount = soulData.getSummonedSouls().size();
        int maxSummons = getMaxSummons(player);
        
        if (summonedCount >= maxSummons) {
            player.sendMessage("§cYou can only have " + maxSummons + " souls summoned at once!");
            return false;
        }
        
        // Summon the soul
        soul.setSummoned(true);
        soul.setSummonLocation(player.getLocation());
        soul.setSummonTime(System.currentTimeMillis());
        
        // Spawn the entity
        Entity summoned = spawnSummonedEntity(player, soul);
        soul.setSummonedEntity(summoned);
        
        // Set cooldown
        cooldowns.put(soulId, System.currentTimeMillis());
        
        // Apply hunger effect
        applySummonHunger(player);
        
        // Update scoreboard
        plugin.getScoreboardManager().updateMainScoreboard(player);
        
        // Play summon effects
        playSummonEffects(player, soul);
        
        player.sendMessage("§a✦ Summoned: " + soul.getRank().getDisplayName() + " §f" + soul.getName());
        return true;
    }
    
    /**
     * Recall a specific soul
     */
    public void recallSoul(Player player, UUID soulId) {
        PlayerSoulData soulData = playerSouls.get(player.getUniqueId());
        if (soulData == null) return;
        
        CapturedSoul soul = soulData.getSoulById(soulId);
        if (soul == null || !soul.isSummoned()) return;
        
        // Remove the summoned entity
        if (soul.getSummonedEntity() != null) {
            soul.getSummonedEntity().remove();
        }
        
        soul.setSummoned(false);
        soul.setSummonedEntity(null);
        
        // Play recall effects
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.5f);
        
        player.sendMessage("§d✦ Recalled: " + soul.getName());
    }
    
    /**
     * Recall all summoned souls
     */
    public void recallAllSouls(Player player) {
        PlayerSoulData soulData = playerSouls.get(player.getUniqueId());
        if (soulData == null) return;
        
        int recalled = 0;
        for (CapturedSoul soul : soulData.getSummonedSouls()) {
            if (soul.getSummonedEntity() != null) {
                soul.getSummonedEntity().remove();
            }
            soul.setSummoned(false);
            soul.setSummonedEntity(null);
            recalled++;
        }
        
        if (recalled > 0) {
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1.0f, 0.5f);
            player.sendMessage("§d✦ Recalled §f" + recalled + " §dsouls");
        }
    }
    
    /**
     * Heal a soul
     */
    public boolean healSoul(Player player, String soulName, int amount) {
        PlayerSoulData soulData = playerSouls.get(player.getUniqueId());
        if (soulData == null) return false;
        
        CapturedSoul soul = soulData.getSoulByName(soulName);
        if (soul == null) return false;
        
        double oldHealth = soul.getCurrentHealth();
        soul.heal(amount);
        
        // If soul is summoned, update its health
        if (soul.isSummoned() && soul.getSummonedEntity() instanceof Mob mob) {
            mob.setHealth(Math.min(mob.getMaxHealth(), mob.getHealth() + amount));
        }
        
        player.sendMessage("§a✓ Healed " + soul.getName() + " §a(" + 
            (int)oldHealth + " → " + (int)soul.getCurrentHealth() + " HP)");
        
        return true;
    }
    
    /**
     * Release a soul (permanently)
     */
    public boolean releaseSoul(Player player, String soulName) {
        PlayerSoulData soulData = playerSouls.get(player.getUniqueId());
        if (soulData == null) return false;
        
        CapturedSoul soul = soulData.getSoulByName(soulName);
        if (soul == null) return false;
        
        // Remove summoned entity if active
        if (soul.isSummoned() && soul.getSummonedEntity() != null) {
            soul.getSummonedEntity().remove();
        }
        
        // Remove from data
        boolean removed = soulData.removeSoul(soul.getOriginalId());
        
        if (removed) {
            player.sendMessage("§cReleased soul: " + soul.getName());
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_DEATH, 1.0f, 0.5f);
        }
        
        return removed;
    }
    
    /**
     * Add kill to queue for capture
     */
    public void addKillToCaptureQueue(Player killer, LivingEntity entity) {
        killQueue.computeIfAbsent(killer.getUniqueId(), k -> new ArrayList<>()).add(entity);
        
        // Keep only last 5 kills
        List<LivingEntity> kills = killQueue.get(killer.getUniqueId());
        if (kills.size() > 5) {
            kills.remove(0);
        }
    }
    
    /**
     * Attempt capture from chat command
     */
    public void attemptChatCapture(Player player) {
        List<LivingEntity> recentKills = killQueue.get(player.getUniqueId());
        
        if (recentKills == null || recentKills.isEmpty()) {
            player.sendMessage("§cNo recent kills to capture! Kill a mob first.");
            return;
        }
        
        LivingEntity target = recentKills.get(recentKills.size() - 1);
        attemptCapture(player, target);
    }
    
    /**
     * Get player's soul data
     */
    public PlayerSoulData getPlayerSoulData(Player player) {
        return playerSouls.computeIfAbsent(player.getUniqueId(), k -> new PlayerSoulData(player.getUniqueId()));
    }
    
    /**
     * Get all player souls data
     */
    public Map<UUID, PlayerSoulData> getPlayerSouls() {
        return playerSouls;
    }
    
    /**
     * Spawn summoned entity
     */
    private Entity spawnSummonedEntity(Player player, CapturedSoul soul) {
        Location loc = player.getLocation().add(2, 0, 0); // Spawn 2 blocks away
        EntityType type = soul.getEntityType();
        
        // If type is invalid or null, use default based on rank
        if (type == null || type == EntityType.UNKNOWN) {
            type = getDefaultTypeForRank(soul.getRank());
        }
        
        Entity entity = player.getWorld().spawnEntity(loc, type);
        
        if (entity instanceof Mob mob) {
            // Set name and health
            String healthBar = getHealthBar(soul.getCurrentHealth(), soul.getMaxHealth());
            mob.setCustomName(soul.getRank().getDisplayName() + " §f" + soul.getName() + " " + healthBar);
            mob.setCustomNameVisible(true);
            
            // Set max health
            mob.setMaxHealth(soul.getMaxHealth());
            mob.setHealth(soul.getCurrentHealth());
            
            // Make it follow owner
            mob.setTarget(null);
            
            // Add health bar task
            new HealthBarTask(plugin, mob, soul).runTaskTimer(plugin, 0L, 20L);
            
            // Add follow task
            new SoulFollowTask(plugin, player, mob).runTaskTimer(plugin, 0L, 10L);
        }
        
        return entity;
    }
    
    /**
     * Apply hunger effect when souls are summoned
     */
    private void applySummonHunger(Player player) {
        new BukkitRunnable() {
            int count = 0;
            
            @Override
            public void run() {
                // Check if player still has summoned souls
                PlayerSoulData soulData = playerSouls.get(player.getUniqueId());
                if (soulData == null || soulData.getSummonedSouls().isEmpty()) {
                    cancel();
                    return;
                }
                
                // Increase hunger faster based on number of summoned souls
                int summonedCount = soulData.getSummonedSouls().size();
                int hungerReduction = Math.min(4, summonedCount); // Max 4 hunger per second
                
                player.setFoodLevel(Math.max(0, player.getFoodLevel() - hungerReduction));
                player.setSaturation(Math.max(0, player.getSaturation() - hungerReduction));
                
                // Show warning when hunger is low
                if (player.getFoodLevel() < 6) {
                    player.sendMessage("§c⚠ Your hunger is draining fast due to summoned souls!");
                }
                
                count++;
                
                // Stop after 60 seconds if no souls
                if (count >= 60 && (soulData == null || soulData.getSummonedSouls().isEmpty())) {
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 20L, 20L); // Every second
    }
    
    /**
     * Play summon effects
     */
    private void playSummonEffects(Player player, CapturedSoul soul) {
        Location loc = player.getLocation();
        
        // Spiral particles
        new BukkitRunnable() {
            int tick = 0;
            
            @Override
            public void run() {
                if (tick >= 20) {
                    cancel();
                    return;
                }
                
                double radius = 2.0;
                double angle = tick * 0.5;
                
                for (int i = 0; i < 4; i++) {
                    double particleAngle = angle + (i * Math.PI / 2);
                    double x = Math.cos(particleAngle) * radius;
                    double z = Math.sin(particleAngle) * radius;
                    
                    Location particleLoc = player.getLocation().clone().add(x, 1, z);
                    player.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, particleLoc, 1, 0, 0, 0, 0);
                }
                
                tick++;
            }
        }.runTaskTimer(plugin, 0L, 2L);
        
        // Sound effects
        player.getWorld().playSound(loc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
        player.getWorld().playSound(loc, Sound.ENTITY_WITHER_SPAWN, 0.3f, 1.5f);
    }
    
    /**
     * Spawn capture success particles
     */
    private void spawnCaptureParticles(Location location) {
        location.getWorld().spawnParticle(Particle.PORTAL, location, 100, 1, 1, 1, 0.5);
        location.getWorld().spawnParticle(Particle.SOUL, location, 50, 0.5, 1, 0.5, 0.1);
        location.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, location, 30, 1, 1, 1, 0.1);
        location.getWorld().playSound(location, Sound.ENTITY_WITHER_SPAWN, 1.0f, 1.0f);
    }
    
    /**
     * Spawn capture failure particles
     */
    private void spawnFailureParticles(Location location) {
        location.getWorld().spawnParticle(Particle.SMOKE_LARGE, location, 50, 1, 1, 1, 0.1);
        location.getWorld().spawnParticle(Particle.ANGRY_VILLAGER, location, 20, 1, 1, 1, 0.1);
        location.getWorld().spawnParticle(Particle.LAVA, location, 10, 1, 1, 1, 0.1);
        location.getWorld().playSound(location, Sound.ENTITY_WITHER_HURT, 1.0f, 0.5f);
    }
    
    /**
     * Determine soul rank based on entity
     */
    private SoulRank determineSoulRank(Entity entity) {
        if (entity instanceof Player) return SoulRank.PLAYER;
        if (entity instanceof EnderDragon) return SoulRank.BOSS;
        if (entity instanceof Wither) return SoulRank.BOSS;
        if (entity instanceof Warden) return SoulRank.BOSS;
        if (entity instanceof Ghast) return SoulRank.BOSS;
        if (entity instanceof Ravager) return SoulRank.BOSS;
        if (entity instanceof ElderGuardian) return SoulRank.BOSS;
        
        if (entity instanceof WitherSkeleton) return SoulRank.ELITE;
        if (entity instanceof PiglinBrute) return SoulRank.ELITE;
        if (entity instanceof Evoker) return SoulRank.ELITE;
        if (entity instanceof Vindicator) return SoulRank.ELITE;
        if (entity instanceof Pillager) return SoulRank.ELITE;
        
        if (entity instanceof Monster) return SoulRank.WARRIOR;
        if (entity instanceof Animals) return SoulRank.NORMAL;
        
        return SoulRank.NORMAL;
    }
    
    /**
     * Get default entity type for rank
     */
    private EntityType getDefaultTypeForRank(SoulRank rank) {
        return switch (rank) {
            case BOSS -> EntityType.WITHER_SKELETON;
            case ELITE -> EntityType.VINDICATOR;
            case WARRIOR -> EntityType.ZOMBIE;
            case PLAYER -> EntityType.PLAYER;
            default -> EntityType.ZOMBIE;
        };
    }
    
    /**
     * Get soul cooldown based on rank
     */
    private long getSoulCooldown(CapturedSoul soul) {
        return switch (soul.getRank()) {
            case BOSS -> 60000; // 60 seconds
            case ELITE -> 30000; // 30 seconds
            case WARRIOR -> 15000; // 15 seconds
            case PLAYER -> 45000; // 45 seconds
            default -> 5000; // 5 seconds
        };
    }
    
    /**
     * Get max summons based on player rank/level
     */
    private int getMaxSummons(Player player) {
        // You can implement a leveling system here
        // For now, base on permission
        if (player.hasPermission("solarise.vip")) {
            return 5;
        }
        if (player.hasPermission("solarise.premium")) {
            return 3;
        }
        return 1; // Default: 1 soul
    }
    
    /**
     * Get health bar string
     */
    private String getHealthBar(double current, double max) {
        int bars = 10;
        int healthBars = (int) ((current / max) * bars);
        
        StringBuilder bar = new StringBuilder("§c");
        for (int i = 0; i < healthBars; i++) bar.append("❤");
        bar.append("§8");
        for (int i = healthBars; i < bars; i++) bar.append("❤");
        
        return bar.toString();
    }
    
    /**
     * Inner class for soul follow task
     */
    private static class SoulFollowTask extends BukkitRunnable {
        private final SoloArisePlugin plugin;
        private final Player player;
        private final Mob mob;
        
        public SoulFollowTask(SoloArisePlugin plugin, Player player, Mob mob) {
            this.plugin = plugin;
            this.player = player;
            this.mob = mob;
        }
        
        @Override
        public void run() {
            if (mob.isDead() || !mob.isValid()) {
                cancel();
                return;
            }
            
            // Follow player if too far
            if (mob.getLocation().distance(player.getLocation()) > 10) {
                mob.teleport(player.getLocation().add(2, 0, 2));
            }
            
            // Attack player's target
            if (player.getTargetEntity(5) instanceof LivingEntity target && 
                target != mob && 
                !target.equals(player)) {
                mob.setTarget(target);
            }
        }
    }
}
