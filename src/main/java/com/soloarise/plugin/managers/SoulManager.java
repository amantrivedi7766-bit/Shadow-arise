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
    private final Map<UUID, Map<UUID, Integer>> captureAttempts = new ConcurrentHashMap<>();
    private final Map<UUID, List<LivingEntity>> killQueue = new ConcurrentHashMap<>();
    private final Map<UUID, Map<UUID, Boolean>> permanentFailures = new ConcurrentHashMap<>();
    
    public SoulManager(SoloArisePlugin plugin) {
        this.plugin = plugin;
    }
    
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
        
        if (currentAttempts >= 1) {
            player.sendMessage("§cYou can only attempt to capture a soul once!");
            return false;
        }
        
        // 50% success, 50% fail chance
        boolean success = Math.random() < 0.5;
        
        if (success) {
            // Successful capture
            SoulRank rank = determineSoulRank(target);
            CapturedSoul soul = new CapturedSoul(target.getUniqueId(), target.getName(), rank);
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
    
    private void spawnCaptureParticles(Location location) {
        location.getWorld().spawnParticle(Particle.PORTAL, location, 100, 1, 1, 1, 0.5);
        location.getWorld().spawnParticle(Particle.SOUL, location, 50, 0.5, 1, 0.5, 0.1);
        location.getWorld().playSound(location, Sound.ENTITY_WITHER_SPAWN, 1.0f, 1.0f);
    }
    
    private void spawnFailureParticles(Location location) {
        location.getWorld().spawnParticle(Particle.LARGE_SMOKE, location, 50, 1, 1, 1, 0.1);
        location.getWorld().spawnParticle(Particle.ANGRY_VILLAGER, location, 20, 1, 1, 1, 0.1);
        location.getWorld().playSound(location, Sound.ENTITY_WITHER_HURT, 1.0f, 0.5f);
    }
    
    private SoulRank determineSoulRank(Entity entity) {
        if (entity instanceof Player) return SoulRank.PLAYER;
        if (entity instanceof EnderDragon) return SoulRank.BOSS;
        if (entity instanceof Wither) return SoulRank.BOSS;
        if (entity instanceof Warden) return SoulRank.BOSS;
        if (entity instanceof Ghast) return SoulRank.BOSS;
        if (entity instanceof WitherSkeleton) return SoulRank.ELITE;
        if (entity instanceof Monster) return SoulRank.WARRIOR;
        return SoulRank.NORMAL;
    }
    
    public boolean summonSoul(Player player, UUID soulId) {
        PlayerSoulData soulData = playerSouls.get(player.getUniqueId());
        if (soulData == null) return false;
        
        CapturedSoul soul = soulData.getSoulById(soulId);
        if (soul == null) return false;
        
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
        
        // Summon the soul
        soul.setSummoned(true);
        soul.setSummonLocation(player.getLocation());
        soul.setSummonTime(System.currentTimeMillis());
        
        // Spawn the entity
        Entity summoned = spawnSummonedEntity(player, soul);
        soul.setSummonedEntity(summoned);
        
        // Apply hunger effect
        applySummonHunger(player);
        
        // Update scoreboard
        plugin.getScoreboardManager().updateMainScoreboard(player);
        
        // Play effects
        playSummonEffects(player, soul);
        
        player.sendMessage("§a✦ Summoned: " + soul.getRank().getDisplayName() + " §f" + soul.getName());
        return true;
    }
    
    private Entity spawnSummonedEntity(Player player, CapturedSoul soul) {
        Location loc = player.getLocation().add(2, 0, 0); // Spawn slightly away
        Entity entity = player.getWorld().spawnEntity(loc, soul.getEntityType());
        
        if (entity instanceof Mob mob) {
            // Set properties
            mob.setCustomName(soul.getDisplayName());
            mob.setCustomNameVisible(true);
            
            // Set health
            mob.setMaxHealth(soul.getMaxHealth());
            mob.setHealth(soul.getCurrentHealth());
            
            // Make it follow player
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!soul.isSummoned() || mob.isDead() || !player.isOnline()) {
                        cancel();
                        return;
                    }
                    
                    // Follow player if too far
                    if (mob.getLocation().distance(player.getLocation()) > 10) {
                        mob.teleport(player.getLocation().add(2, 0, 0));
                    }
                    
                    // Update health in name
                    mob.setCustomName(soul.getDisplayName());
                    
                    // Update soul health
                    soul.setCurrentHealth(mob.getHealth());
                }
            }.runTaskTimer(plugin, 0L, 20L);
        }
        
        return entity;
    }
    
    private void playSummonEffects(Player player, CapturedSoul soul) {
        Location loc = player.getLocation();
        
        // Spiral particles
        for (int i = 0; i < 360; i += 10) {
            double angle = Math.toRadians(i);
            double x = Math.cos(angle) * 2;
            double z = Math.sin(angle) * 2;
            
            loc.clone().add(x, 1, z).getWorld().spawnParticle(
                Particle.SOUL_FIRE_FLAME,
                loc.clone().add(x, 1, z),
                1, 0, 0, 0, 0
            );
        }
        
        // Sound effects
        player.getWorld().playSound(loc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
        player.getWorld().playSound(loc, Sound.ENTITY_WITHER_SPAWN, 0.5f, 1.5f);
    }
    
    private void applySummonHunger(Player player) {
        new BukkitRunnable() {
            int count = 0;
            
            @Override
            public void run() {
                PlayerSoulData soulData = playerSouls.get(player.getUniqueId());
                if (soulData == null || !soulData.hasAnySummoned()) {
                    cancel();
                    return;
                }
                
                if (count >= 60) { // 60 seconds
                    cancel();
                    return;
                }
                
                // Increase hunger faster while souls are summoned
                player.setFoodLevel(Math.max(0, player.getFoodLevel() - 1));
                player.setSaturation(Math.max(0, player.getSaturation() - 1));
                
                count++;
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }
    
    public boolean recallSouls(Player player) {
        PlayerSoulData soulData = playerSouls.get(player.getUniqueId());
        if (soulData == null) return false;
        
        List<CapturedSoul> summoned = soulData.getSummonedSouls();
        if (summoned.isEmpty()) {
            player.sendMessage("§cYou have no summoned souls!");
            return false;
        }
        
        for (CapturedSoul soul : summoned) {
            if (soul.getSummonedEntity() != null) {
                soul.getSummonedEntity().remove();
            }
            soul.setSummoned(false);
            soul.setSummonedEntity(null);
        }
        
        player.sendMessage("§d✦ All souls have been recalled!");
        return true;
    }
    
    public void recallAllSouls() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            recallSouls(player);
        }
    }
    
    public void addKillToCaptureQueue(Player killer, LivingEntity entity) {
        killQueue.computeIfAbsent(killer.getUniqueId(), k -> new ArrayList<>()).add(entity);
        
        // Remove old kills after 30 seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                List<LivingEntity> queue = killQueue.get(killer.getUniqueId());
                if (queue != null) {
                    queue.remove(entity);
                }
            }
        }.runTaskLater(plugin, 600L);
    }
    
    public void markForCapture(Player killer, Player victim) {
        addKillToCaptureQueue(killer, victim);
    }
    
    public boolean attackPlayer(Player attacker, String targetName) {
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) return false;
        
        PlayerSoulData soulData = playerSouls.get(attacker.getUniqueId());
        if (soulData == null) return false;
        
        List<CapturedSoul> summoned = soulData.getSummonedSouls();
        if (summoned.isEmpty()) {
            attacker.sendMessage("§cYou have no summoned souls!");
            return false;
        }
        
        // Make summoned souls attack target
        for (CapturedSoul soul : summoned) {
            if (soul.getSummonedEntity() instanceof Mob mob) {
                mob.setTarget(target);
            }
        }
        
        attacker.sendMessage("§eYour souls are now attacking " + target.getName());
        target.sendMessage("§c" + attacker.getName() + "'s souls are attacking you!");
        return true;
    }
    
    public boolean assignTask(Player player, String soulName, String task) {
        PlayerSoulData soulData = playerSouls.get(player.getUniqueId());
        if (soulData == null) return false;
        
        CapturedSoul soul = soulData.getSoulByName(soulName);
        if (soul == null) return false;
        
        if (!soul.isSummoned()) {
            player.sendMessage("§cThis soul is not summoned!");
            return false;
        }
        
        // Implement task based on task string
        switch(task.toLowerCase()) {
            case "guard":
                // Make soul guard current location
                player.sendMessage("§a" + soul.getName() + " is now guarding this area!");
                break;
            case "follow":
                // Already follows by default
                player.sendMessage("§a" + soul.getName() + " will now follow you!");
                break;
            case "wander":
                // Make soul wander
                player.sendMessage("§a" + soul.getName() + " will now wander!");
                break;
            default:
                player.sendMessage("§cUnknown task! Use: guard, follow, wander");
                return false;
        }
        
        return true;
    }
    
    public boolean orderWork(Player player, String soulName, String workOrder) {
        PlayerSoulData soulData = playerSouls.get(player.getUniqueId());
        if (soulData == null) return false;
        
        CapturedSoul soul = soulData.getSoulByName(soulName);
        if (soul == null) return false;
        
        if (!soul.isSummoned()) {
            player.sendMessage("§cThis soul is not summoned!");
            return false;
        }
        
        // Implement work order
        player.sendMessage("§a" + soul.getName() + " is now working on: " + workOrder);
        return true;
    }
    
    public boolean releaseSoul(Player player, String soulName) {
        PlayerSoulData soulData = playerSouls.get(player.getUniqueId());
        if (soulData == null) return false;
        
        CapturedSoul soul = soulData.getSoulByName(soulName);
        if (soul == null) return false;
        
        if (soul.isSummoned()) {
            if (soul.getSummonedEntity() != null) {
                soul.getSummonedEntity().remove();
            }
        }
        
        boolean removed = soulData.removeSoul(soul.getOriginalId());
        if (removed) {
            player.sendMessage("§cSoul " + soulName + " has been released!");
            plugin.getScoreboardManager().updateMainScoreboard(player);
        }
        
        return removed;
    }
    
    public boolean healSoul(Player player, String soulName) {
        PlayerSoulData soulData = playerSouls.get(player.getUniqueId());
        if (soulData == null) return false;
        
        CapturedSoul soul = soulData.getSoulByName(soulName);
        if (soul == null) return false;
        
        soul.healFully();
        if (soul.getSummonedEntity() instanceof Mob mob) {
            mob.setHealth(soul.getMaxHealth());
        }
        
        player.sendMessage("§a✓ " + soulName + " has been fully healed!");
        return true;
    }
    
    public void attemptChatCapture(Player player) {
        List<LivingEntity> recentKills = killQueue.get(player.getUniqueId());
        
        if (recentKills == null || recentKills.isEmpty()) {
            player.sendMessage("§cNo recent kills to capture! Kill a mob first.");
            return;
        }
        
        LivingEntity target = recentKills.get(recentKills.size() - 1);
        attemptCapture(player, target);
    }
    
    // Getters
    public Map<UUID, PlayerSoulData> getPlayerSouls() {
        return playerSouls;
    }
    
    public PlayerSoulData getPlayerSoulData(Player player) {
        return playerSouls.get(player.getUniqueId());
    }
        }
