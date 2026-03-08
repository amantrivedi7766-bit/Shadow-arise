package com.soloarise.plugin.models;

import org.bukkit.entity.Player;
import java.util.UUID;

public class Task {
    
    private final UUID id;
    private final String name;
    private final String description;
    private final TaskType type;
    private final int requirement;
    private final long duration; // in milliseconds
    
    public Task(String name, String description, TaskType type, int requirement, long duration) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.description = description;
        this.type = type;
        this.requirement = requirement;
        this.duration = duration;
    }
    
    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public TaskType getType() { return type; }
    public int getRequirement() { return requirement; }
    public long getDuration() { return duration; }
    
    public boolean isComplete(Player player, ArisePlayer arisePlayer) {
        switch(type) {
            case KILL_MOBS:
                return arisePlayer.getMobsKilled() >= requirement;  // Fixed method name
            case MINE_BLOCKS:
                return arisePlayer.getBlocksMined() >= requirement;
            case COLLECT_SOULS:
                return arisePlayer.getSoulsCollected() >= requirement;  // Fixed method name
            case TRAVEL_DISTANCE:
                return arisePlayer.getDistanceTraveled() >= requirement;
            case CRAFT_ITEMS:
                return arisePlayer.getItemsCrafted() >= requirement;
            case FISH_ITEMS:
                return arisePlayer.getFishCaught() >= requirement;
            default:
                return false;
        }
    }
    
    public enum TaskType {
        KILL_MOBS,
        MINE_BLOCKS,
        COLLECT_SOULS,
        TRAVEL_DISTANCE,
        CRAFT_ITEMS,
        FISH_ITEMS
    }
}
