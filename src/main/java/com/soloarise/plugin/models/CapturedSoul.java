package com.soloarise.plugin.models;

import java.util.UUID;

public class CapturedSoul {
    
    private final UUID originalId;
    private final String name;
    private final SoulRank rank;
    private int energy = 100;
    private final int maxEnergy = 100;
    
    public CapturedSoul(UUID originalId, String name, SoulRank rank) {
        this.originalId = originalId;
        this.name = name;
        this.rank = rank;
    }
    
    public UUID getOriginalId() { return originalId; }
    public String getName() { return name; }
    public SoulRank getRank() { return rank; }
    public int getEnergy() { return energy; }
    public int getMaxEnergy() { return maxEnergy; }
    
    public boolean consumeEnergy(int amount) {
        if (energy >= amount) {
            energy -= amount;
            return true;
        }
        return false;
    }
    
    public boolean hasEnergy(int amount) {
        return energy >= amount;
    }
    
    public void heal(int amount) {
        energy = Math.min(maxEnergy, energy + amount);
    }
    
    public String getGroupName() {
        if (name.toLowerCase().contains("pig")) return "pig";
        if (name.toLowerCase().contains("zombie")) return "zombie";
        if (name.toLowerCase().contains("skeleton")) return "skeleton";
        if (name.toLowerCase().contains("spider")) return "spider";
        if (name.toLowerCase().contains("creeper")) return "creeper";
        return name.toLowerCase();
    }
}
