package com.soloarise.plugin.managers;

import com.soloarise.plugin.SoloArisePlugin;
import com.soloarise.plugin.models.*;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerManager {
    
    private final SoloArisePlugin plugin;
    private final Map<UUID, ArisePlayer> players = new ConcurrentHashMap<>();
    private final Set<UUID> activeTasks = new HashSet<>();
    
    public PlayerManager(SoloArisePlugin plugin) {
        this.plugin = plugin;
    }
    
    public ArisePlayer getPlayer(Player player) {
        return players.computeIfAbsent(player.getUniqueId(), k -> new ArisePlayer(player));
    }
    
    public Map<UUID, ArisePlayer> getAllPlayers() {
        return players;
    }
    
    public void assignTask(Player player) {
        ArisePlayer arisePlayer = getPlayer(player);
        
        if (arisePlayer.hasArisePower()) {
            return;
        }
        
        if (activeTasks.contains(player.getUniqueId())) {
            return;
        }
        
        Task task = plugin.getTaskManager().getUniqueTask();
        if (task != null) {
            arisePlayer.setCurrentTask(task);
            arisePlayer.setTaskStartTime(System.currentTimeMillis());
            activeTasks.add(player.getUniqueId());
            
            // Show boss bar
            plugin.getTaskManager().showTaskBossBar(player, task);
            
            // Show task scoreboard
            plugin.getScoreboardManager().showTaskScoreboard(player);
            
            player.sendMessage("§a✦ New Task Assigned: §e" + task.getName());
            player.sendMessage("§7Complete this task within 1 hour to unlock Arise power!");
        }
    }
    
    public boolean completeTask(Player player) {
        ArisePlayer arisePlayer = getPlayer(player);
        
        if (!arisePlayer.hasActiveTask()) {
            return false;
        }
        
        Task task = arisePlayer.getCurrentTask();
        
        if (!task.isComplete(player, arisePlayer)) {
            player.sendMessage("§cYou haven't completed the task yet!");
            return false;
        }
        
        if (System.currentTimeMillis() - arisePlayer.getTaskStartTime() > 3600000) {
            player.sendMessage("§cYour task has expired!");
            activeTasks.remove(player.getUniqueId());
            arisePlayer.setCurrentTask(null);
            plugin.getScoreboardManager().removeTaskScoreboard(player);
            return false;
        }
        
        // Unlock arise power
        arisePlayer.setHasArisePower(true);
        arisePlayer.setCurrentTask(null);
        activeTasks.remove(player.getUniqueId());
        
        // Remove boss bar and task scoreboard
        plugin.getTaskManager().removeTaskBossBar(player);
        plugin.getScoreboardManager().removeTaskScoreboard(player);
        
        // Start ceremony
        startCeremony(player);
        
        return true;
    }
    
    private void startCeremony(Player player) {
        Location startLoc = player.getLocation();
        
        player.sendMessage("§d✦ §5§lCEREMONY OF AWAKENING §d✦");
        player.sendMessage("§7You have unlocked the Arise power!");
        
        new BukkitRunnable() {
            int tick = 0;
            double height = 0;
            
            @Override
            public void run() {
                if (tick >= 100) { // 5 seconds
                    cancel();
                    player.sendMessage("§a✓ You are now an Awakened being!");
                    plugin.getScoreboardManager().updateMainScoreboard(player);
                    return;
                }
                
                // Slowly rise up to 5 blocks
                if (tick < 40) {
                    height = tick * 0.125; // 5 blocks in 40 ticks
                    Location newLoc = startLoc.clone().add(0, height, 0);
                    player.teleport(newLoc);
                }
                
                // Orbital particles
                double radius = 2.0;
                double angle = tick * 0.2;
                
                for (int i = 0; i < 4; i++) {
                    double particleAngle = angle + (i * Math.PI / 2);
                    double x = Math.cos(particleAngle) * radius;
                    double z = Math.sin(particleAngle) * radius;
                    
                    Location particleLoc = player.getLocation().clone().add(x, 1, z);
                    player.getWorld().spawnParticle(Particle.SPELL_WITCH, particleLoc, 5, 0, 0, 0, 0.5);
                    player.getWorld().spawnParticle(Particle.PORTAL, particleLoc, 10, 0.2, 0.2, 0.2, 0.1);
                }
                
                // Cinematic effects
                player.getWorld().spawnParticle(Particle.FLASH, player.getLocation(), 1);
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                
                if (tick % 10 == 0) {
                    player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1.0f, 1.0f);
                }
                
                tick++;
            }
        }.runTaskTimer(plugin, 0L, 2L); // Run every 2 ticks
    }
    
    public void removeActiveTask(UUID playerId) {
        activeTasks.remove(playerId);
    }
}
