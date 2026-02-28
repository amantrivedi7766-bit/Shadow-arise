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
        
        // Check max souls limit
        if (soulData.getSouls().size() >= plugin.getConfigManager().getMaxSoulsPerPlayer()) {
            player.sendMessage("§cYou have reached the maximum number of souls! Release some souls first.");
            return false;
        }
        
        // Check capture attempts
        Map<UUID, Integer> attempts = captureAttempts.computeIfAbsent(player.getUniqueId(), k -> new ConcurrentHashMap<>());
        int currentAttempts = attempts.getOrDefault(target.getUniqueId(), 0);
        
        if (currentAttempts >= plugin.getConfigManager().getMaxAttempts()) {
            player.sendMessage("§cYou have failed to capture this soul " + plugin.getConfigManager().getMaxAttempts() + " times. It is now lost forever!");
            return false;
        }
        
        // Calculate capture chance based on target type
        double captureChance;
        if (isBossMob(target)) {
            captureChance = plugin.getConfigManager().getBossCaptureChance();
        } else {
            captureChance = plugin.getConfigManager().getCaptureChance();
        }
        
        // Capture chance logic
        boolean success = Math.random() < captureChance;
        
        if (success) {
            // Successful capture
            SoulRank rank = determineSoulRank(target);
            CapturedSoul soul = new CapturedSoul(target.getUniqueId(), target.getName(), rank);
            soulData.addSoul(soul);
            
            // Reset attempts on success
            attempts.remove(target.getUniqueId());
            
            // Spawn particles
            plugin.getParticleManager().spawnCaptureParticles(target.getLocation());
            
            player.sendMessage("§a✓ Successfully captured " + target.getName() + "'s soul!");
            player.sendMessage("§7Rank: " + rank.getDisplayName());
            
            // Remove the entity if it's a mob
            if (target instanceof Mob) {
                target.remove();
            }
            
            // Update scoreboard
            plugin.getScoreboardManager().updateScoreboard(player);
            
            return true;
        } else {
            // Failed capture
            attempts.put(target.getUniqueId(), currentAttempts + 1);
            player.sendMessage("§cFailed to capture soul. Attempt " + (currentAttempts + 1) + "/" + plugin.getConfigManager().getMaxAttempts());
            
            if (currentAttempts + 1 >= plugin.getConfigManager().getMaxAttempts()) {
                player.sendMessage("§4This soul is now lost forever!");
            }
            
            return false;
        }
    }
    
    private boolean isBossMob(Entity entity) {
        return entity instanceof EnderDragon ||
               entity instanceof Wither ||
               entity instanceof Warden ||
               entity instanceof Ghast ||
               entity instanceof ElderGuardian;
    }
    
    private SoulRank determineSoulRank(Entity entity) {
        if (entity instanceof Player) {
            return SoulRank.PLAYER;
        }
        
        if (entity instanceof EnderDragon) return SoulRank.BOSS;
        if (entity instanceof Wither) return SoulRank.BOSS;
        if (entity instanceof Warden) return SoulRank.BOSS;
        if (entity instanceof Ghast) return SoulRank.BOSS;
        if (entity instanceof ElderGuardian) return SoulRank.BOSS;
        
        if (entity instanceof WitherSkeleton) return SoulRank.ELITE;
        if (entity instanceof PiglinBrute) return SoulRank.ELITE;
        if (entity instanceof Ravager) return SoulRank.ELITE;
        
        if (entity instanceof Monster) return SoulRank.WARRIOR;
        if (entity instanceof Animals) return SoulRank.NORMAL;
        
        return SoulRank.NORMAL;
    }
    
    public CapturedSoul getSoulByName(Player player, String name) {
        PlayerSoulData soulData = playerSouls.get(player.getUniqueId());
        if (soulData == null) return null;
        
        return soulData.getSoulByName(name);
    }
    
    public List<CapturedSoul> getSoulsByGroup(Player player, String groupName) {
        PlayerSoulData soulData = playerSouls.get(player.getUniqueId());
        if (soulData == null) return new ArrayList<>();
        
        return soulData.getSoulsByGroup(groupName);
    }
    
    public boolean releaseSoul(Player player, String identifier) {
        PlayerSoulData soulData = playerSouls.get(player.getUniqueId());
        if (soulData == null) return false;
        
        boolean released = soulData.removeSoul(identifier);
        if (released) {
            plugin.getScoreboardManager().updateScoreboard(player);
        }
        return released;
    }
    
    public boolean orderWork(Player player, String soulName, String workOrder) {
        CapturedSoul soul = getSoulByName(player, soulName);
        if (soul == null) {
            player.sendMessage("§cSoul not found!");
            return false;
        }
        
        // Deduct soul energy
        if (!soul.consumeEnergy(plugin.getConfigManager().getWorkEnergyCost())) {
            player.sendMessage("§cThis soul doesn't have enough energy! Craft a soul healer to recharge.");
            player.sendMessage("§7Energy: " + soul.getEnergy() + "/" + soul.getMaxEnergy());
            return false;
        }
        
        // Execute work order
        executeWorkOrder(player, soul, workOrder);
        
        // Spawn work particles
        plugin.getParticleManager().spawnSoulWorkParticles(player.getLocation(), workOrder);
        
        return true;
    }
    
    private void executeWorkOrder(Player player, CapturedSoul soul, String workOrder) {
        // Simple work order implementation
        player.sendMessage("§a✦ " + soul.getRank().getDisplayName() + " §f" + soul.getName() + " §ais now working on: §e" + workOrder);
        player.sendMessage("§7Energy remaining: " + soul.getEnergy() + "/" + soul.getMaxEnergy());
        
        // Here you can add specific work order logic
        // For example: mining, building, farming, etc.
    }
    
    public boolean healSoul(Player player, String soulName, int diamonds) {
        CapturedSoul soul = getSoulByName(player, soulName);
        if (soul == null) {
            player.sendMessage("§cSoul not found!");
            return false;
        }
        
        // Check if player has enough diamonds
        if (!player.getInventory().contains(org.bukkit.Material.DIAMOND, diamonds)) {
            player.sendMessage("§cYou need " + diamonds + " diamonds to heal this soul!");
            return false;
        }
        
        // Remove diamonds
        player.getInventory().removeItem(new org.bukkit.inventory.ItemStack(org.bukkit.Material.DIAMOND, diamonds));
        
        // Heal soul
        int healAmount = diamonds * plugin.getConfigManager().getHealAmountPerDiamond();
        soul.heal(healAmount);
        
        player.sendMessage("§a✓ " + soul.getRank().getDisplayName() + " §f" + soul.getName() + " §ahas been healed!");
        player.sendMessage("§7Energy: " + soul.getEnergy() + "/" + soul.getMaxEnergy());
        
        return true;
    }
    
    public boolean attackPlayer(Player attacker, String targetPlayerName) {
        Player target = plugin.getServer().getPlayer(targetPlayerName);
        if (target == null) {
            attacker.sendMessage("§cPlayer not found or offline!");
            return false;
        }
        
        if (target.equals(attacker)) {
            attacker.sendMessage("§cYou cannot attack yourself!");
            return false;
        }
        
        PlayerSoulData soulData = playerSouls.get(attacker.getUniqueId());
        if (soulData == null || soulData.getSouls().isEmpty()) {
            attacker.sendMessage("§cYou have no souls to attack with!");
            return false;
        }
        
        // Select strongest souls for attack (BOSS first, then ELITE, then WARRIOR)
        List<CapturedSoul> attackingSouls = new ArrayList<>();
        
        // Add BOSS souls
        attackingSouls.addAll(soulData.getSoulsByRank(SoulRank.BOSS));
        
        // Add ELITE souls if needed
        if (attackingSouls.size() < 3) {
            attackingSouls.addAll(soulData.getSoulsByRank(SoulRank.ELITE));
        }
        
        // Add WARRIOR souls if needed
        if (attackingSouls.size() < 3) {
            attackingSouls.addAll(soulData.getSoulsByRank(SoulRank.WARRIOR));
        }
        
        // Limit to 3 souls
        if (attackingSouls.size() > 3) {
            attackingSouls = attackingSouls.subList(0, 3);
        }
        
        // Check if any souls have enough energy
        attackingSouls.removeIf(s -> !s.hasEnergy(plugin.getConfigManager().getAttackEnergyCost()));
        
        if (attackingSouls.isEmpty()) {
            attacker.sendMessage("§cYour souls don't have enough energy to attack!");
            return false;
        }
        
        // Launch attack animation
        launchSoulAttack(attacker, target, attackingSouls);
        return true;
    }
    
    private void launchSoulAttack(Player attacker, Player target, List<CapturedSoul> souls) {
        attacker.sendMessage("§c⚔ Attacking §e" + target.getName() + " §cwith " + souls.size() + " souls!");
        target.sendMessage("§c⚔ You are being attacked by §e" + attacker.getName() + "'s §csouls!");
        
        new BukkitRunnable() {
            int count = 0;
            
            @Override
            public void run() {
                if (count >= souls.size()) {
                    attacker.sendMessage("§a✓ Attack completed!");
                    cancel();
                    return;
                }
                
                CapturedSoul soul = souls.get(count);
                
                // Damage effect
                target.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, target.getLocation().add(0, 1, 0), 20, 0.5, 1, 0.5, 0.1);
                target.getWorld().playSound(target.getLocation(), Sound.ENTITY_WITHER_SHOOT, 0.5f, 1.0f);
                
                // Calculate damage based on soul rank
                double damage = 2.0 + (soul.getRank().getPowerLevel() * 1.5);
                target.damage(damage, attacker);
                
                // Consume energy
                soul.consumeEnergy(plugin.getConfigManager().getAttackEnergyCost());
                
                count++;
            }
        }.runTaskTimer(plugin, 0L, 15L); // Attack every 0.75 seconds
    }
    
    public void recallSouls(Player player) {
        PlayerSoulData soulData = playerSouls.get(player.getUniqueId());
        if (soulData == null || soulData.getSouls().isEmpty()) {
            player.sendMessage("§cYou have no souls to recall!");
            return;
        }
        
        // Spiral particle effect for recall
        plugin.getParticleManager().spawnRecallParticles(player);
        
        player.sendMessage("§d✦ All souls have returned to shadow state!");
        player.sendMessage("§7Total souls recalled: §f" + soulData.getSouls().size());
    }
    
    public boolean assignTask(Player player, String identifier, String task) {
        List<CapturedSoul> souls = new ArrayList<>();
        
        // Check if identifier is a group or individual soul
        CapturedSoul singleSoul = getSoulByName(player, identifier);
        if (singleSoul != null) {
            souls.add(singleSoul);
        } else {
            souls = getSoulsByGroup(player, identifier);
        }
        
        if (souls.isEmpty()) {
            player.sendMessage("§cNo souls found with name/group: " + identifier);
            return false;
        }
        
        // Assign task to each soul
        for (CapturedSoul soul : souls) {
            if (soul.hasEnergy(5)) {
                soul.consumeEnergy(5);
                player.sendMessage("§a✓ Task assigned to " + soul.getRank().getDisplayName() + " §f" + soul.getName() + "§a: §e" + task);
            } else {
                player.sendMessage("§c" + soul.getName() + " doesn't have enough energy!");
            }
        }
        
        return true;
    }
    
    public void addKillToCaptureQueue(Player killer, LivingEntity entity) {
        killQueue.computeIfAbsent(killer.getUniqueId(), k -> new ArrayList<>()).add(entity);
        
        // Remove from queue after 30 seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                List<LivingEntity> queue = killQueue.get(killer.getUniqueId());
                if (queue != null) {
                    queue.remove(entity);
                }
            }
        }.runTaskLater(plugin, 600L); // 30 seconds = 600 ticks
    }
    
    public void attemptChatCapture(Player player) {
        List<LivingEntity> recentKills = killQueue.get(player.getUniqueId());
        
        if (recentKills == null || recentKills.isEmpty()) {
            player.sendMessage("§cNo recent kills to capture! Kill a mob first.");
            return;
        }
        
        // Get the most recent kill
        LivingEntity target = recentKills.get(recentKills.size() - 1);
        attemptCapture(player, target);
    }
    
    public void markForCapture(Player victim, Player killer) {
        // This will be used when a player dies
        // The killer can capture the victim's soul within a time window
        addKillToCaptureQueue(killer, victim);
        killer.sendMessage("§d⚔ " + victim.getName() + " has fallen! Type 'arise' in chat to capture their soul!");
    }
    
    // Getters
    public Map<UUID, PlayerSoulData> getPlayerSouls() {
        return playerSouls;
    }
    
    public Map<UUID, PlayerSoulData> getAllSoulData() {
        return playerSouls;
    }
          }
