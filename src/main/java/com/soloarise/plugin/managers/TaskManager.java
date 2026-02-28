package com.soloarise.plugin.managers;

import com.soloarise.plugin.SoloArisePlugin;
import com.soloarise.plugin.models.ArisePlayer;
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
    private final Set<Integer> usedTaskIds = new HashSet<>();
    
    public TaskManager(SoloArisePlugin plugin) {
        this.plugin = plugin;
        loadTasks();
    }
    
    private void loadTasks() {
        // 60+ unique tasks
        availableTasks.add(new Task(1, "Mine 64 Diamonds", 
            (p, ap) -> p.getInventory().contains(org.bukkit.Material.DIAMOND, 64)));
        
        availableTasks.add(new Task(2, "Kill 50 Zombies", 
            (p, ap) -> ap.getKillCount("ZOMBIE") >= 50));
        
        availableTasks.add(new Task(3, "Travel 5000 Blocks", 
            (p, ap) -> ap.getDistanceTraveled() >= 5000));
        
        availableTasks.add(new Task(4, "Catch 20 Fish", 
            (p, ap) -> ap.getFishCaught() >= 20));
        
        availableTasks.add(new Task(5, "Brew 10 Potions", 
            (p, ap) -> ap.getPotionsBrewed() >= 10));
        
        availableTasks.add(new Task(6, "Enchant 15 Items", 
            (p, ap) -> ap.getItemsEnchanted() >= 15));
        
        availableTasks.add(new Task(7, "Trade with Villagers 30 times", 
            (p, ap) -> ap.getVillagerTrades() >= 30));
        
        availableTasks.add(new Task(8, "Plant and Harvest 100 Crops", 
            (p, ap) -> ap.getCropsHarvested() >= 100));
        
        availableTasks.add(new Task(9, "Defeat the Ender Dragon", 
            (p, ap) -> ap.hasDefeatedEnderDragon()));
        
        availableTasks.add(new Task(10, "Collect 16 Gold Blocks", 
            (p, ap) -> p.getInventory().contains(org.bukkit.Material.GOLD_BLOCK, 16)));
        
        // Add 50 more tasks
        for (int i = 11; i <= 60; i++) {
            availableTasks.add(generateRandomTask(i));
        }
    }
    
    private Task generateRandomTask(int id) {
        String[] tasks = {
            "Kill 30 Skeletons", "Mine 100 Iron Ore", "Craft 50 Torches",
            "Cook 64 Steaks", "Shear 20 Sheep", "Milk 10 Cows",
            "Ride a Pig 100 blocks", "Climb a Mountain (Y=150)", "Dive to Ocean Floor",
            "Find a Desert Temple", "Locate a Jungle Temple", "Defeat a Raid",
            "Tame 5 Wolves", "Breed 10 Animals", "Fly with Elytra 1000 blocks",
            "Survive a Fall from Height", "Swim 1000 blocks", "Craft a Beacon",
            "Find Ancient Debris", "Create a Nether Portal", "Trade with Piglins 20 times",
            "Defeat a Wither Skeleton", "Collect Blaze Rods", "Find a Stronghold",
            "Open an Ender Chest", "Use a Grindstone 10 times", "Smith 15 Tools",
            "Fish a Treasure Item", "Find a Shipwreck", "Locate a Buried Treasure",
            "Ride a Strider", "Explore a Bastion", "Visit a Nether Fortress",
            "Collect 32 Quartz", "Mine 16 Ancient Debris", "Create 10 Soul Torches",
            "Harvest Honey from 5 Beehives", "Plant 50 Saplings", "Trade with a Wandering Trader",
            "Defeat a Ravager", "Kill 10 Vindicators", "Collect 20 Emeralds",
            "Build a Iron Golem", "Create a Snow Golem", "Light a Campfire 10 times",
            "Bake 32 Bread", "Make 16 Cookies", "Craft a Cake",
            "Brew a Potion of Strength", "Brew a Potion of Swiftness", "Catch 10 Tropical Fish",
            "Find a Coral Reef", "Explore a Cave", "Mine 256 Cobblestone",
            "Kill 40 Spiders", "Kill 25 Creepers", "Kill 15 Endermen",
            "Enter the Nether", "Enter the End", "Collect 32 Leather",
            "Collect 64 String", "Collect 32 Gunpowder", "Collect 16 Ender Pearls"
        };
        
        int index = (id - 11) % tasks.length;
        String taskName = tasks[index];
        
        return new Task(id, taskName, (p, ap) -> {
            // Simple completion check for random tasks
            if (taskName.contains("Kill")) return ap.getKillCount("ZOMBIE") >= 30;
            if (taskName.contains("Mine")) return ap.getBlocksMined() >= 100;
            if (taskName.contains("Craft")) return ap.getItemsCrafted() >= 50;
            return Math.random() < 0.3; // Placeholder
        });
    }
    
    public Task getUniqueTask() {
        List<Task> unusedTasks = availableTasks.stream()
            .filter(t -> !usedTaskIds.contains(t.getId()))
            .toList();
        
        if (unusedTasks.isEmpty()) {
            // Reset used tasks if all have been used
            usedTaskIds.clear();
            unusedTasks = availableTasks;
        }
        
        if (!unusedTasks.isEmpty()) {
            Task task = unusedTasks.get(new Random().nextInt(unusedTasks.size()));
            usedTaskIds.add(task.getId());
            return task;
        }
        
        return availableTasks.get(0); // Fallback
    }
    
    public void showTaskBossBar(Player player, Task task) {
        BossBar bossBar = Bukkit.createBossBar(
            "§6✦ Task: §e" + task.getName() + " §7[0%]",
            BarColor.PURPLE,
            BarStyle.SEGMENTED_10
        );
        
        bossBar.addPlayer(player);
        playerBossBars.put(player.getUniqueId(), bossBar);
    }
    
    public void updateTaskBossBar(Player player, Task task, double progress) {
        BossBar bossBar = playerBossBars.get(player.getUniqueId());
        if (bossBar != null) {
            bossBar.setProgress(Math.min(1.0, progress));
            bossBar.setTitle("§6✦ Task: §e" + task.getName() + " §7[" + (int)(progress * 100) + "%]");
        }
    }
    
    public void removeTaskBossBar(Player player) {
        BossBar bossBar = playerBossBars.remove(player.getUniqueId());
        if (bossBar != null) {
            bossBar.removeAll();
        }
    }
    
    public void updateAllBossBars() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            ArisePlayer arisePlayer = plugin.getPlayerManager().getPlayer(player);
            if (arisePlayer.hasActiveTask()) {
                Task task = arisePlayer.getCurrentTask();
                long elapsed = System.currentTimeMillis() - arisePlayer.getTaskStartTime();
                double progress = Math.min(1.0, elapsed / 3600000.0); // 1 hour = 3600000 ms
                
                if (elapsed > 3600000) {
                    // Task expired
                    player.sendMessage("§cYour task has expired!");
                    plugin.getPlayerManager().removeActiveTask(player.getUniqueId());
                    removeTaskBossBar(player);
                } else {
                    updateTaskBossBar(player, task, progress);
                }
            }
        }
    }
}
