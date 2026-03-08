package com.soloarise.plugin.models;

import org.bukkit.entity.Player;
import java.util.UUID;

public class ArisePlayer {
    
    private final UUID playerId;
    private final String playerName;
    private boolean hasArisePower = false;
    private Task currentTask = null;
    private long taskStartTime = 0;
    
    // Task progress tracking
    private int mobsKilled = 0;
    private int blocksMined = 0;
    private int soulsCollected = 0;
    private int distanceTraveled = 0;
    private int itemsCrafted = 0;
    private int fishCaught = 0;
    
    public ArisePlayer(Player player) {
        this.playerId = player.getUniqueId();
        this.playerName = player.getName();
    }
    
    // Getters and Setters
    public UUID getPlayerId() { return playerId; }
    public String getPlayerName() { return playerName; }
    public boolean hasArisePower() { return hasArisePower; }
    public void setHasArisePower(boolean hasArisePower) { this.hasArisePower = hasArisePower; }
    
    public boolean hasActiveTask() { return currentTask != null; }
    public Task getCurrentTask() { return currentTask; }
    public void setCurrentTask(Task task) { this.currentTask = task; }
    public long getTaskStartTime() { return taskStartTime; }
    public void setTaskStartTime(long time) { this.taskStartTime = time; }
    
    // Task progress getters
    public int getMobsKilled() { return mobsKilled; }
    public void setMobsKilled(int count) { this.mobsKilled = count; }
    public void incrementMobsKilled() { this.mobsKilled++; }
    
    public int getBlocksMined() { return blocksMined; }
    public void setBlocksMined(int count) { this.blocksMined = count; }
    public void incrementBlocksMined() { this.blocksMined++; }
    
    public int getSoulsCollected() { return soulsCollected; }
    public void setSoulsCollected(int count) { this.soulsCollected = count; }
    public void incrementSoulsCollected() { this.soulsCollected++; }
    
    public int getDistanceTraveled() { return distanceTraveled; }
    public void setDistanceTraveled(int distance) { this.distanceTraveled = distance; }
    public void addDistanceTraveled(int distance) { this.distanceTraveled += distance; }
    
    public int getItemsCrafted() { return itemsCrafted; }
    public void setItemsCrafted(int count) { this.itemsCrafted = count; }
    public void incrementItemsCrafted() { this.itemsCrafted++; }
    
    public int getFishCaught() { return fishCaught; }
    public void setFishCaught(int count) { this.fishCaught = count; }
    public void incrementFishCaught() { this.fishCaught++; }
    
    // Reset all progress
    public void resetProgress() {
        mobsKilled = 0;
        blocksMined = 0;
        soulsCollected = 0;
        distanceTraveled = 0;
        itemsCrafted = 0;
        fishCaught = 0;
    }
    
    // Get progress for current task
    public int getTaskProgress() {
        if (currentTask == null) return 0;
        
        return switch (currentTask.getType()) {
            case KILL_MOBS -> mobsKilled;
            case MINE_BLOCKS -> blocksMined;
            case COLLECT_SOULS -> soulsCollected;
            case TRAVEL_DISTANCE -> distanceTraveled;
            case CRAFT_ITEMS -> itemsCrafted;
            case FISH_ITEMS -> fishCaught;
        };
    }
}
