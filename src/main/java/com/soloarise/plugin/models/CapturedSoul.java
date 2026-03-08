package com.soloarise.plugin.models;

import com.soloarise.plugin.SoloArisePlugin;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.UUID;

public class CapturedSoul {
    
    private final UUID soulId;
    private final String mobType;
    private final String originalName;
    private final SoulRank rank;
    private final UUID ownerId;
    
    // Stats
    private double currentHealth;
    private double maxHealth;
    private int energy = 100;
    private final int maxEnergy = 100;
    
    // Summon status
    private boolean summoned = false;
    private Entity summonedEntity;
    private Location summonLocation;
    private long summonTime;
    
    // Abilities
    private Ability ability;
    private int abilityCooldown = 0;
    
    // Hunger multiplier (when summoned)
    private static final double HUNGER_MULTIPLIER = 2.0; // 2x faster hunger
    
    public CapturedSoul(UUID soulId, String mobType, String originalName, SoulRank rank, Location capturedLocation) {
        this.soulId = soulId;
        this.mobType = mobType;
        this.originalName = originalName;
        this.rank = rank;
        this.ownerId = null; // Will be set when added to player
        
        // Set health based on rank
        this.maxHealth = calculateMaxHealth(rank);
        this.currentHealth = this.maxHealth;
        
        // Set ability based on mob type
        this.ability = createAbility(mobType);
    }
    
    private double calculateMaxHealth(SoulRank rank) {
        return switch(rank) {
            case NORMAL -> 20.0;      // 10 hearts
            case WARRIOR -> 40.0;     // 20 hearts
            case ELITE -> 80.0;        // 40 hearts
            case BOSS -> 200.0;         // 100 hearts
            case PLAYER -> 100.0;       // 50 hearts
        };
    }
    
    private Ability createAbility(String mobType) {
        String lowerType = mobType.toLowerCase();
        
        return switch(lowerType) {
            // Hostile mobs
            case "zombie" -> new Ability("Infection", "Poisons enemies on hit", 5000);
            case "skeleton" -> new Ability("Volley", "Shoots 3 arrows rapidly", 3000);
            case "spider" -> new Ability("Web Trap", "Slows enemies with webs", 4000);
            case "creeper" -> new Ability("Explosive Charge", "Explodes when near multiple enemies", 10000);
            case "enderman" -> new Ability("Teleport Strike", "Teleports behind enemies", 3000);
            case "blaze" -> new Ability("Fire Storm", "Shoots fireballs at enemies", 5000);
            case "wither_skeleton" -> new Ability("Wither Strike", "Applies wither effect", 6000);
            case "ghast" -> new Ability("Fireball Barrage", "Shoots multiple fireballs", 12000);
            case "magma_cube" -> new Ability("Lava Splash", "Splashes lava on enemies", 8000);
            case "slime" -> new Ability("Split", "Splits into smaller slimes", 7000);
            case "witch" -> new Ability("Potion Throw", "Throws harmful potions", 4000);
            case "pillager" -> new Ability("Crossbarrage", "Shoots rapid crossbow shots", 3000);
            case "vindicator" -> new Ability("Chop", "Powerful axe attack", 5000);
            case "evoker" -> new Ability("Fang Attack", "Summons fangs from ground", 8000);
            case "ravager" -> new Ability("Stomp", "Stomps the ground damaging area", 10000);
            case "warden" -> new Ability("Sonic Boom", "Long range sonic attack", 15000);
            case "ender_dragon" -> new Ability("Dragon Breath", "Breathes fire in an area", 20000);
            case "wither" -> new Ability("Wither Skulls", "Shoots explosive skulls", 15000);
            
            // Passive mobs
            case "wolf" -> new Ability("Pack Leader", "Summons temporary wolves", 15000);
            case "iron_golem" -> new Ability("Shield Bash", "Knocks back and stuns enemies", 8000);
            case "snowman" -> new Ability("Snowball Barrage", "Throws snowballs rapidly", 2000);
            case "bee" -> new Ability("Sting", "Poisons and damages enemies", 3000);
            case "dolphin" -> new Ability("Speed Boost", "Gives speed to owner", 10000);
            case "horse" -> new Ability("Charge", "Dashes forward damaging enemies", 5000);
            case "llama" -> new Ability("Spit", "Spits at enemies from range", 2000);
            case "parrot" -> new Ability("Distraction", "Distracts enemies with sound", 4000);
            case "fox" -> new Ability("Steal", "Steals items from enemies", 6000);
            case "goat" -> new Ability("Ram", "Knocks back enemies with ram", 4000);
            case "frog" -> new Ability("Leap", "Leaps onto enemies", 3000);
            case "allay" -> new Ability("Collect", "Collects item drops for owner", 5000);
            case "axolotl" -> new Ability("Regeneration", "Heals owner over time", 10000);
            case "turtle" -> new Ability("Shell Defense", "Reduces damage taken", 8000);
            case "panda" -> new Ability("Sneeze", "Sneezes knocking back enemies", 5000);
            case "polar_bear" -> new Ability("Slash", "Powerful claw attack", 6000);
            
            // Default
            default -> new Ability("Basic Attack", "Standard melee attack", 2000);
        };
    }
    
    // ===== GETTERS =====
    public UUID getSoulId() { return soulId; }
    public String getMobType() { return mobType; }
    public String getOriginalName() { return originalName; }
    public SoulRank getRank() { return rank; }
    public UUID getOwnerId() { return ownerId; }
    
    public double getCurrentHealth() { return currentHealth; }
    public double getMaxHealth() { return maxHealth; }
    public int getEnergy() { return energy; }
    public int getMaxEnergy() { return maxEnergy; }
    
    public boolean isSummoned() { return summoned; }
    public Entity getSummonedEntity() { return summonedEntity; }
    public Location getSummonLocation() { return summonLocation; }
    public long getSummonTime() { return summonTime; }
    
    public Ability getAbility() { return ability; }
    public int getAbilityCooldown() { return abilityCooldown; }
    
    public static double getHungerMultiplier() { return HUNGER_MULTIPLIER; }
    
    // ===== SETTERS =====
    public void setCurrentHealth(double health) {
        this.currentHealth = Math.min(maxHealth, Math.max(0, health));
    }
    
    public void setMaxHealth(double health) {
        this.maxHealth = health;
        if (this.currentHealth > health) {
            this.currentHealth = health;
        }
    }
    
    public void setEnergy(int energy) {
        this.energy = Math.min(maxEnergy, Math.max(0, energy));
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
    
    public void setAbilityCooldown(int cooldown) {
        this.abilityCooldown = cooldown;
    }
    
    // ===== UTILITY METHODS =====
    
    /**
     * Heal the soul by specified amount
     */
    public void heal(double amount) {
        this.currentHealth = Math.min(maxHealth, currentHealth + amount);
        
        // Update summoned entity if exists
        if (summoned && summonedEntity instanceof LivingEntity living) {
            living.setHealth(currentHealth);
        }
    }
    
    /**
     * Heal the soul to full health
     */
    public void healFully() {
        this.currentHealth = maxHealth;
        this.energy = maxEnergy;
        
        // Update summoned entity if exists
        if (summoned && summonedEntity instanceof LivingEntity living) {
            living.setHealth(maxHealth);
        }
    }
    
    /**
     * Damage the soul
     */
    public void damage(double amount) {
        this.currentHealth = Math.max(0, currentHealth - amount);
    }
    
    /**
     * Check if soul is dead
     */
    public boolean isDead() {
        return currentHealth <= 0;
    }
    
    /**
     * Consume energy
     */
    public boolean consumeEnergy(int amount) {
        if (energy >= amount) {
            energy -= amount;
            return true;
        }
        return false;
    }
    
    /**
     * Get formatted name for display
     */
    public String getFormattedName() {
        String[] words = mobType.toLowerCase().split("_");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (word.length() > 0) {
                result.append(Character.toUpperCase(word.charAt(0)))
                      .append(word.substring(1)).append(" ");
            }
        }
        return result.toString().trim();
    }
    
    /**
     * Get colored name based on rank
     */
    public String getColoredName() {
        String color = switch(rank) {
            case NORMAL -> "§7";
            case WARRIOR -> "§a";
            case ELITE -> "§9";
            case BOSS -> "§c";
            case PLAYER -> "§d";
        };
        return color + getFormattedName();
    }
    
    /**
     * Get display name with health bar
     */
    public String getDisplayName() {
        return getColoredName() + " §f" + getHealthBar();
    }
    
    /**
     * Generate health bar
     */
    public String getHealthBar() {
        int bars = 10;
        int healthBars = (int) ((currentHealth / maxHealth) * bars);
        
        StringBuilder bar = new StringBuilder("§c");
        for (int i = 0; i < healthBars; i++) bar.append("❤");
        bar.append("§8");
        for (int i = healthBars; i < bars; i++) bar.append("❤");
        
        return bar.toString();
    }
    
    /**
     * Get compact health bar (for scoreboard)
     */
    public String getCompactHealthBar() {
        int bars = 5;
        int healthBars = (int) ((currentHealth / maxHealth) * bars);
        
        StringBuilder bar = new StringBuilder("§c");
        for (int i = 0; i < healthBars; i++) bar.append("●");
        bar.append("§7");
        for (int i = healthBars; i < bars; i++) bar.append("●");
        
        return bar.toString();
    }
    
    /**
     * Check if ability is ready
     */
    public boolean isAbilityReady() {
        return abilityCooldown <= 0;
    }
    
    /**
     * Update cooldown (call every tick)
     */
    public void tickCooldown() {
        if (abilityCooldown > 0) {
            abilityCooldown--;
        }
    }
    
    /**
     * Reset ability cooldown
     */
    public void resetAbilityCooldown() {
        this.abilityCooldown = ability.getCooldown() / 50; // Convert ms to ticks (20 ticks/sec)
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CapturedSoul that = (CapturedSoul) obj;
        return soulId.equals(that.soulId);
    }
    
    @Override
    public int hashCode() {
        return soulId.hashCode();
    }
        }
