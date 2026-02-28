package com.soloarise.plugin.managers;

import com.soloarise.plugin.SoloArisePlugin;
import com.soloarise.plugin.models.*;
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
            return; // Player already has arise power
        }
        
        if (activeTasks.contains(player.getUniqueId())) {
            return; // Player already has an active task
        }
        
        // Get a unique task that hasn't been assigned to any player
        Task task = plugin.getTaskManager().getUniqueTask();
        if (task != null) {
            arisePlayer.setCurrentTask(task);
            arisePlayer.setTaskStartTime(System.currentTimeMillis());
            activeTasks.add(player.getUniqueId());
            
            // Show boss bar to player
            plugin.getTaskManager().showTaskBossBar(player, task);
            
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
        
        // Check if task is complete
        if (!task.isComplete(player, arisePlayer)) {
            player.sendMessage("§cYou haven't completed the task yet!");
            return false;
        }
        
        // Check if task expired (1 hour = 3600000 ms)
        if (System.currentTimeMillis() - arisePlayer.getTaskStartTime() > 3600000) {
            player.sendMessage("§cYour task has expired! Ask an admin for a new task.");
            activeTasks.remove(player.getUniqueId());
            arisePlayer.setCurrentTask(null);
            return false;
        }
        
        // Unlock arise power
        arisePlayer.setHasArisePower(true);
        arisePlayer.setCurrentTask(null);
        activeTasks.remove(player.getUniqueId());
        
        // Remove boss bar
        plugin.getTaskManager().removeTaskBossBar(player);
        
        player.sendMessage("§a✓ Congratulations! You have unlocked the Arise power!");
        player.sendMessage("§7Type §e/arise §7to claim your power!");
        
        return true;
    }
    
    public void removeActiveTask(UUID playerId) {
        activeTasks.remove(playerId);
    }
}
