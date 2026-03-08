package com.soloarise.plugin.models;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
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
    private long summonTime;
    private EntityType entityType;
    private String groupName;
    
    public CapturedSoul(UUID originalId, String name, SoulRank rank) {
        this.originalId = originalId;
        this.name = name;
        this.rank = rank;
        this.maxHealth = rank.getPowerLevel() * 20;
        this.currentHealth = this.maxHealth;
        this.groupName = determineGroupName(name);
        this.entityType = determineEntityType(name);
    }
    
    private String determineGroupName(String entityName) {
        String lowerName = entityName.toLowerCase();
        if (lowerName.contains("zombie")) return "zombie";
        if (lowerName.contains("skeleton")) return "skeleton";
        if (lowerName.contains("spider")) return "spider";
        if (lowerName.contains("creeper")) return "creeper";
        if (lowerName.contains("pig")) return "pig";
        if (lowerName.contains("cow")) return "cow";
        if (lowerName.contains("sheep")) return "sheep";
        if (lowerName.contains("wolf")) return "wolf";
        if (lowerName.contains("villager")) return "villager";
        return "misc";
    }
    
    private EntityType determineEntityType(String entityName) {
        String lowerName = entityName.toLowerCase();
        if (lowerName.contains("zombie")) return EntityType.ZOMBIE;
        if (lowerName.contains("skeleton")) return EntityType.SKELETON;
        if (lowerName.contains("spider")) return EntityType.SPIDER;
        if (lowerName.contains("creeper")) return EntityType.CREEPER;
        if (lowerName.contains("pig")) return EntityType.PIG;
        if (lowerName.contains("cow")) return EntityType.COW;
        if (lowerName.contains("sheep")) return EntityType.SHEEP;
        if (lowerName.contains("wolf")) return EntityType.WOLF;
        if (lowerName.contains("villager")) return EntityType.VILLAGER;
        return EntityType.ZOMBIE; // Default
    }
    
    // Getters
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
    public long getSummonTime() { return summonTime; }
    public EntityType getEntityType() { return entityType; }
    public String getGroupName() { return groupName; }
    
    // Setters
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
            this.summonTime = System.currentTimeMillis();
        }
    }
    
    public void setSummonLocation(Location location) {
        this.summonLocation = location;
    }
    
    public void setSummonTime(long time) {
        this.summonTime = time;
    }
    
    public void setEntityType(EntityType type) {
        this.entityType = type;
    }
    
    public void setMaxHealth(double health) {
        this.maxHealth = health;
        if (this.currentHealth > health) {
            this.currentHealth = health;
        }
    }
    
    // Utility methods
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
    
    public void healFully() {
        energy = maxEnergy;
        currentHealth = maxHealth;
    }
    
    public boolean isDead() {
        return currentHealth <= 0;
    }
    
    public String getHealthBar() {
        int bars = 10;
        int healthBars = (int) ((currentHealth / maxHealth) * bars);
        
        StringBuilder bar = new StringBuilder("§c");
        for (int i = 0; i < healthBars; i++) bar.append("❤");
        bar.append("§8");
        for (int i = healthBars; i < bars; i++) bar.append("❤");
        
        return bar.toString();
    }
    
    public String getDisplayName() {
        String color = switch(rank) {
            case NORMAL -> "§7";
            case WARRIOR -> "§a";
            case ELITE -> "§9";
            case BOSS -> "§c";
            case PLAYER -> "§d";
        };
        return color + name + " §f" + getHealthBar();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CapturedSoul that = (CapturedSoul) obj;
        return originalId.equals(that.originalId);
    }
    
    @Override
    public int hashCode() {
        return originalId.hashCode();
    }
}
