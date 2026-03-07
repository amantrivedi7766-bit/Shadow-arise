package com.soloarise.plugin.models;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import java.util.UUID;

public class CapturedSoul {
    
    private final UUID originalId;
    private final String name;
    private final SoulRank rank;
    private int energy = 100;
    private final int maxEnergy = 100;
    private double currentHealth;
    private double maxHealth;
    private boolean summoned = false;
    private Entity summonedEntity;
    private Location summonLocation;
    
    public CapturedSoul(UUID originalId, String name, SoulRank rank) {
        this.originalId = originalId;
        this.name = name;
        this.rank = rank;
        this.maxHealth = rank.getPowerLevel() * 20;
        this.currentHealth = this.maxHealth;
    }
    
    public UUID getOriginalId() { return originalId; }
    public String getName() { return name; }
    public SoulRank getRank() { return rank; }
    public int getEnergy() { return energy; }
    public int getMaxEnergy() { return maxEnergy; }
    public double getCurrentHealth() { return currentHealth; }
    public double getMaxHealth() { return maxHealth; }
    public boolean isSummoned() { return summoned; }
    public Entity getSummonedEntity() { return summonedEntity; }
    public Location getSummonLocation() { return summonLocation; }
    
    public void setCurrentHealth(double health) {
        this.currentHealth = Math.min(maxHealth, Math.max(0, health));
    }
    
    public void setSummoned(boolean summoned) {
        this.summoned = summoned;
    }
    
    public void setSummonedEntity(Entity entity) {
        this.summonedEntity = entity;
        if (entity != null) {
            this.summonLocation = entity.getLocation();
        }
    }
    
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
        currentHealth = Math.min(maxHealth, currentHealth + amount);
    }
    
    public String getGroupName() {
        if (name.toLowerCase().contains("pig")) return "pig";
        if (name.toLowerCase().contains("zombie")) return "zombie";
        if (name.toLowerCase().contains("skeleton")) return "skeleton";
        return name.toLowerCase();
    }
}
