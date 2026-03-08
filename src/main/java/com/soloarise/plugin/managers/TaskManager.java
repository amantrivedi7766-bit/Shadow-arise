package com.soloarise.plugin.managers;

import com.soloarise.plugin.SoloArisePlugin;
import com.soloarise.plugin.models.Task;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TaskManager {
    
    private final SoloArisePlugin plugin;
    private final List<Task> availableTasks = new ArrayList<>();
    private final Map<UUID, BossBar> playerBossBars = new ConcurrentHashMap<>();
    private final Random random = new Random();
    
    public TaskManager(SoloArisePlugin plugin) {
        this.plugin = plugin;
        loadTasks();
    }
    
    private void loadTasks() {
        // Add default tasks
        availableTasks.add(new Task(
            "Novice Hunter", 
            "Kill 10 monsters", 
            Task.TaskType.KILL_MOBS, 
            10, 
            3600000
        ));
        
        availableTasks.add(new Task(
            "Miner", 
            "Mine 50 stone blocks", 
            Task.TaskType.MINE_BLOCKS, 
            50, 
            3600000
        ));
        
        availableTasks.add(new Task(
            "Soul Collector", 
            "Collect 5 souls", 
            Task.TaskType.COLLECT_SOULS, 
            5, 
            3600000
        ));
        
        availableTasks.add(new Task(
            "Explorer", 
            "Travel 1000 blocks", 
            Task.TaskType.TRAVEL_DISTANCE, 
            1000, 
            3600000
        ));
        
        availableTasks.add(new Task(
            "Artisan", 
            "Craft 20 items", 
            Task.TaskType.CRAFT_ITEMS, 
            20, 
            3600000
        ));
        
        availableTasks.add(new Task(
            "Fisherman", 
            "Catch 10 fish", 
            Task.TaskType.FISH_ITEMS, 
            10, 
            3600000
        ));
    }
    
    public Task getUniqueTask() {
        if (availableTasks.isEmpty()) return null;
        return availableTasks.get(random.nextInt(availableTasks.size()));
    }
    
    public void showTaskBossBar(Player player, Task task) {
        BossBar bar = Bukkit.createBossBar(
            "§6§lTask: §e" + task.getName(),
            BarColor.YELLOW,
            BarStyle.SOLID
        );
        bar.addPlayer(player);
        bar.setProgress(0.0);
        playerBossBars.put(player.getUniqueId(), bar);
    }
    
    public void removeTaskBossBar(Player player) {
        BossBar bar = playerBossBars.remove(player.getUniqueId());
        if (bar != null) {
            bar.removeAll();
        }
    }
    
    public void updateAllBossBars() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            BossBar bar = playerBossBars.get(player.getUniqueId());
            if (bar != null) {
                var arisePlayer = plugin.getPlayerManager().getPlayer(player);
                if (arisePlayer.hasActiveTask()) {
                    Task task = arisePlayer.getCurrentTask();
                    long elapsed = System.currentTimeMillis() - arisePlayer.getTaskStartTime();
                    double progress = Math.min(1.0, elapsed / (double) task.getDuration());
                    bar.setProgress(progress);
                    
                    // Update title with remaining time
                    long remaining = Math.max(0, task.getDuration() - elapsed);
                    int minutes = (int) (remaining / 60000);
                    int seconds = (int) ((remaining % 60000) / 1000);
                    bar.setTitle("§6§lTask: §e" + task.getName() + " §7[" + 
                        String.format("%02d:%02d", minutes, seconds) + "]");
                }
            }
        }
    }
    
    public List<Task> getAvailableTasks() {
        return new ArrayList<>(availableTasks);
    }
}
