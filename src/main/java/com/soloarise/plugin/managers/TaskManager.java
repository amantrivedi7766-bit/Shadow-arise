package com.soloarise.plugin.managers;

import com.soloarise.plugin.SoloArisePlugin;
import com.soloarise.plugin.models.ArisePlayer;
import com.soloarise.plugin.models.Task;
import com.soloarise.plugin.models.TaskCompletionChecker;
import org.bukkit.Bukkit;
import org.bukkit.Material;
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
        // Task 1: Mine 64 Diamonds
        availableTasks.add(new Task(1, "Mine 64 Diamonds", new TaskCompletionChecker() {
            @Override
            public boolean isComplete(Player player, ArisePlayer arisePlayer) {
                return player.getInventory().contains(Material.DIAMOND, 64);
            }
        }));
        
        // Task 2: Kill 50 Zombies
        availableTasks.add(new Task(2, "Kill 50 Zombies", new TaskCompletionChecker() {
            @Override
            public boolean isComplete(Player player, ArisePlayer arisePlayer) {
                return arisePlayer.getKillCount("ZOMBIE") >= 50;
            }
        }));
        
        // Task 3: Travel 5000 Blocks
        availableTasks.add(new Task(3, "Travel 5000 Blocks", new TaskCompletionChecker() {
            @Override
            public boolean isComplete(Player player, ArisePlayer arisePlayer) {
                return arisePlayer.getDistanceTraveled() >= 5000;
            }
        }));
        
        // Task 4: Catch 20 Fish
        availableTasks.add(new Task(4, "Catch 20 Fish", new TaskCompletionChecker() {
            @Override
            public boolean isComplete(Player player, ArisePlayer arisePlayer) {
                return arisePlayer.getFishCaught() >= 20;
            }
        }));
        
        // Task 5: Brew 10 Potions
        availableTasks.add(new Task(5, "Brew 10 Potions", new TaskCompletionChecker() {
            @Override
            public boolean isComplete(Player player, ArisePlayer arisePlayer) {
                return arisePlayer.getPotionsBrewed() >= 10;
            }
        }));
        
        // Task 6: Enchant 15 Items
        availableTasks.add(new Task(6, "Enchant 15 Items", new TaskCompletionChecker() {
            @Override
            public boolean isComplete(Player player, ArisePlayer arisePlayer) {
                return arisePlayer.getItemsEnchanted() >= 15;
            }
        }));
        
        // Task 7: Trade with Villagers 30 times
        availableTasks.add(new Task(7, "Trade with Villagers 30 times", new TaskCompletionChecker() {
            @Override
            public boolean isComplete(Player player, ArisePlayer arisePlayer) {
                return arisePlayer.getVillagerTrades() >= 30;
            }
        }));
        
        // Task 8: Plant and Harvest 100 Crops
        availableTasks.add(new Task(8, "Plant and Harvest 100 Crops", new TaskCompletionChecker() {
            @Override
            public boolean isComplete(Player player, ArisePlayer arisePlayer) {
                return arisePlayer.getCropsHarvested() >= 100;
            }
        }));
        
        // Task 9: Defeat the Ender Dragon
        availableTasks.add(new Task(9, "Defeat the Ender Dragon", new TaskCompletionChecker() {
            @Override
            public boolean isComplete(Player player, ArisePlayer arisePlayer) {
                return arisePlayer.hasDefeatedEnderDragon();
            }
        }));
        
        // Task 10: Collect 16 Gold Blocks
        availableTasks.add(new Task(10, "Collect 16 Gold Blocks", new TaskCompletionChecker() {
            @Override
            public boolean isComplete(Player player, ArisePlayer arisePlayer) {
                return player.getInventory().contains(Material.GOLD_BLOCK, 16);
            }
        }));
        
        // Add 50 more tasks using lambda expressions (Java 8+)
        addMoreTasks();
    }
    
    private void addMoreTasks() {
        // Using lambda expressions for cleaner code
        availableTasks.add(new Task(11, "Kill 30 Skeletons", 
            (player, ap) -> ap.getKillCount("SKELETON") >= 30));
        
        availableTasks.add(new Task(12, "Mine 100 Iron Ore", 
            (player, ap) -> ap.getBlocksMined() >= 100));
        
        availableTasks.add(new Task(13, "Craft 50 Torches", 
            (player, ap) -> ap.getItemsCrafted() >= 50));
        
        availableTasks.add(new Task(14, "Cook 64 Steaks", 
            (player, ap) -> player.getInventory().contains(Material.COOKED_BEEF, 64)));
        
        availableTasks.add(new Task(15, "Shear 20 Sheep", 
            (player, ap) -> false)); // Placeholder - implement actual tracking
        
        availableTasks.add(new Task(16, "Milk 10 Cows", 
            (player, ap) -> false)); // Placeholder
        
        availableTasks.add(new Task(17, "Ride a Pig 100 blocks", 
            (player, ap) -> false)); // Placeholder
        
        availableTasks.add(new Task(18, "Climb a Mountain (Y=150)", 
            (player, ap) -> player.getLocation().getY() >= 150));
        
        availableTasks.add(new Task(19, "Dive to Ocean Floor", 
            (player, ap) -> player.getLocation().getY() <= 30 && player.getWorld().getEnvironment() == org.bukkit.World.Environment.NORMAL));
        
        availableTasks.add(new Task(20, "Find a Desert Temple", 
            (player, ap) -> false)); // Placeholder
        
        // Add more tasks up to 60
        String[] taskNames = {
            "Kill 40 Spiders", "Kill 25 Creepers", "Kill 15 Endermen",
            "Enter the Nether", "Enter the End", "Collect 32 Leather",
            "Collect 64 String", "Collect 32 Gunpowder", "Collect 16 Ender Pearls",
            "Find a Jungle Temple", "Defeat a Raid", "Tame 5 Wolves",
            "Breed 10 Animals", "Fly with Elytra 1000 blocks", "Survive a Fall from Height",
            "Swim 1000 blocks", "Craft a Beacon", "Find Ancient Debris",
            "Create a Nether Portal", "Trade with Piglins 20 times", "Defeat a Wither Skeleton",
            "Collect Blaze Rods", "Find a Stronghold", "Open an Ender Chest",
            "Use a Grindstone 10 times", "Smith 15 Tools", "Fish a Treasure Item",
            "Find a Shipwreck", "Locate a Buried Treasure", "Ride a Strider",
            "Explore a Bastion", "Visit a Nether Fortress", "Collect 32 Quartz",
            "Mine 16 Ancient Debris", "Create 10 Soul Torches", "Harvest Honey from 5 Beehives",
            "Plant 50 Saplings", "Trade with a Wandering Trader", "Defeat a Ravager",
            "Kill 10 Vindicators", "Collect 20 Emeralds", "Build an Iron Golem",
            "Create a Snow Golem", "Light a Campfire 10 times", "Bake 32 Bread",
            "Make 16 Cookies", "Craft a Cake", "Brew a Potion of Strength",
            "Brew a Potion of Swiftness", "Catch 10 Tropical Fish", "Find a Coral Reef"
        };
        
        for (int i = 0; i < taskNames.length; i++) {
            int taskId = 21 + i;
            String taskName = taskNames[i];
            
            availableTasks.add(new Task(taskId, taskName, new TaskCompletionChecker() {
                @Override
                public boolean isComplete(Player player, ArisePlayer arisePlayer) {
                    // Simple placeholder - in real implementation, track these stats
                    return Math.random() < 0.3; // 30% chance for testing
                }
            }));
        }
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
