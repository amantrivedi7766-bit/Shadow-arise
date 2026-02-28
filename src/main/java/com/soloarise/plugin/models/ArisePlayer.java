package com.soloarise.plugin.models;

import org.bukkit.entity.Player;
import java.util.UUID;

public class ArisePlayer {
    
    private final UUID playerId;
    private String playerName;
    private boolean hasArisePower = false;
    private Task currentTask = null;
    private long taskStartTime = 0;
    
    // Statistics for task completion
    private int zombieKills = 0;
    private int skeletonKills = 0;
    private int spiderKills = 0;
    private int creeperKills = 0;
    private int endermanKills = 0;
    private int distanceTraveled = 0;
    private int fishCaught = 0;
    private int potionsBrewed = 0;
    private int itemsEnchanted = 0;
    private int villagerTrades = 0;
    private int cropsHarvested = 0;
    private int blocksMined = 0;
    private int itemsCrafted = 0;
    private int animalsBred = 0;
    private boolean defeatedEnderDragon = false;
    private boolean defeatedWither = false;
    private boolean enteredNether = false;
    private boolean enteredEnd = false;
    
    public ArisePlayer(Player player) {
        this.playerId = player.getUniqueId();
        this.playerName = player.getName();
    }
    
    // Basic Getters and Setters
    public UUID getPlayerId() { return playerId; }
    
    public String getPlayerName() { return playerName; }
    
    public void setPlayerName(String playerName) { this.playerName = playerName; }
    
    public boolean hasArisePower() { return hasArisePower; }
    
    public void setHasArisePower(boolean hasArisePower) { this.hasArisePower = hasArisePower; }
    
    public Task getCurrentTask() { return currentTask; }
    
    public void setCurrentTask(Task currentTask) { this.currentTask = currentTask; }
    
    public long getTaskStartTime() { return taskStartTime; }
    
    public void setTaskStartTime(long taskStartTime) { this.taskStartTime = taskStartTime; }
    
    public boolean hasActiveTask() { return currentTask != null; }
    
    // Kill Count Methods
    public int getKillCount(String mobType) {
        switch(mobType.toUpperCase()) {
            case "ZOMBIE": return zombieKills;
            case "SKELETON": return skeletonKills;
            case "SPIDER": return spiderKills;
            case "CREEPER": return creeperKills;
            case "ENDERMAN": return endermanKills;
            default: return 0;
        }
    }
    
    public void incrementZombieKills() { this.zombieKills++; }
    public void incrementSkeletonKills() { this.skeletonKills++; }
    public void incrementSpiderKills() { this.spiderKills++; }
    public void incrementCreeperKills() { this.creeperKills++; }
    public void incrementEndermanKills() { this.endermanKills++; }
    
    // Travel Methods
    public int getDistanceTraveled() { return distanceTraveled; }
    public void addDistanceTraveled(int distance) { this.distanceTraveled += distance; }
    
    // Fishing Methods
    public int getFishCaught() { return fishCaught; }
    public void incrementFishCaught() { this.fishCaught++; }
    
    // Brewing Methods
    public int getPotionsBrewed() { return potionsBrewed; }
    public void incrementPotionsBrewed() { this.potionsBrewed++; }
    
    // Enchanting Methods
    public int getItemsEnchanted() { return itemsEnchanted; }
    public void incrementItemsEnchanted() { this.itemsEnchanted++; }
    
    // Trading Methods
    public int getVillagerTrades() { return villagerTrades; }
    public void incrementVillagerTrades() { this.villagerTrades++; }
    
    // Farming Methods
    public int getCropsHarvested() { return cropsHarvested; }
    public void incrementCropsHarvested() { this.cropsHarvested++; }
    
    // Mining Methods
    public int getBlocksMined() { return blocksMined; }
    public void incrementBlocksMined() { this.blocksMined++; }
    
    // Crafting Methods
    public int getItemsCrafted() { return itemsCrafted; }
    public void incrementItemsCrafted() { this.itemsCrafted++; }
    
    // Breeding Methods
    public int getAnimalsBred() { return animalsBred; }
    public void incrementAnimalsBred() { this.animalsBred++; }
    
    // Boss Methods
    public boolean hasDefeatedEnderDragon() { return defeatedEnderDragon; }
    public void setDefeatedEnderDragon(boolean defeatedEnderDragon) { this.defeatedEnderDragon = defeatedEnderDragon; }
    
    public boolean hasDefeatedWither() { return defeatedWither; }
    public void setDefeatedWither(boolean defeatedWither) { this.defeatedWither = defeatedWither; }
    
    // Dimension Methods
    public boolean hasEnteredNether() { return enteredNether; }
    public void setEnteredNether(boolean enteredNether) { this.enteredNether = enteredNether; }
    
    public boolean hasEnteredEnd() { return enteredEnd; }
    public void setEnteredEnd(boolean enteredEnd) { this.enteredEnd = enteredEnd; }
    
    // Task Progress Calculation
    public double getTaskProgress(Task task) {
        if (task == null) return 0.0;
        
        String taskName = task.getName().toLowerCase();
        
        if (taskName.contains("zombie")) {
            return Math.min(1.0, zombieKills / 50.0);
        } else if (taskName.contains("skeleton")) {
            return Math.min(1.0, skeletonKills / 30.0);
        } else if (taskName.contains("diamond")) {
            // Check inventory for diamonds - this would need player object
            return 0.0; // Placeholder
        } else if (taskName.contains("travel")) {
            return Math.min(1.0, distanceTraveled / 5000.0);
        } else if (taskName.contains("fish")) {
            return Math.min(1.0, fishCaught / 20.0);
        } else if (taskName.contains("potion")) {
            return Math.min(1.0, potionsBrewed / 10.0);
        } else if (taskName.contains("enchant")) {
            return Math.min(1.0, itemsEnchanted / 15.0);
        } else if (taskName.contains("trade")) {
            return Math.min(1.0, villagerTrades / 30.0);
        } else if (taskName.contains("crop") || taskName.contains("harvest")) {
            return Math.min(1.0, cropsHarvested / 100.0);
        } else if (taskName.contains("mine")) {
            return Math.min(1.0, blocksMined / 256.0);
        } else if (taskName.contains("craft")) {
            return Math.min(1.0, itemsCrafted / 50.0);
        } else if (taskName.contains("breed")) {
            return Math.min(1.0, animalsBred / 10.0);
        }
        
        return 0.0;
    }
    
    // Reset all stats (useful for new tasks)
    public void resetStats() {
        zombieKills = 0;
        skeletonKills = 0;
        spiderKills = 0;
        creeperKills = 0;
        endermanKills = 0;
        distanceTraveled = 0;
        fishCaught = 0;
        potionsBrewed = 0;
        itemsEnchanted = 0;
        villagerTrades = 0;
        cropsHarvested = 0;
        blocksMined = 0;
        itemsCrafted = 0;
        animalsBred = 0;
    }
    
    // Save/Load methods
    public String serialize() {
        StringBuilder data = new StringBuilder();
        data.append(playerId).append(";");
        data.append(playerName).append(";");
        data.append(hasArisePower).append(";");
        data.append(taskStartTime).append(";");
        data.append(zombieKills).append(";");
        data.append(skeletonKills).append(";");
        data.append(distanceTraveled).append(";");
        data.append(fishCaught).append(";");
        data.append(potionsBrewed).append(";");
        data.append(itemsEnchanted).append(";");
        data.append(villagerTrades).append(";");
        data.append(cropsHarvested).append(";");
        data.append(defeatedEnderDragon);
        return data.toString();
    }
    
    public static ArisePlayer deserialize(String data, Player player) {
        String[] parts = data.split(";");
        if (parts.length < 13) return null;
        
        ArisePlayer arisePlayer = new ArisePlayer(player);
        arisePlayer.hasArisePower = Boolean.parseBoolean(parts[2]);
        arisePlayer.taskStartTime = Long.parseLong(parts[3]);
        arisePlayer.zombieKills = Integer.parseInt(parts[4]);
        arisePlayer.skeletonKills = Integer.parseInt(parts[5]);
        arisePlayer.distanceTraveled = Integer.parseInt(parts[6]);
        arisePlayer.fishCaught = Integer.parseInt(parts[7]);
        arisePlayer.potionsBrewed = Integer.parseInt(parts[8]);
        arisePlayer.itemsEnchanted = Integer.parseInt(parts[9]);
        arisePlayer.villagerTrades = Integer.parseInt(parts[10]);
        arisePlayer.cropsHarvested = Integer.parseInt(parts[11]);
        arisePlayer.defeatedEnderDragon = Boolean.parseBoolean(parts[12]);
        
        return arisePlayer;
    }
}
