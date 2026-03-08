package com.soloarise.plugin.models;

public class Ability {
    
    private final String name;
    private final String description;
    private final int cooldown; // in milliseconds
    
    public Ability(String name, String description, int cooldown) {
        this.name = name;
        this.description = description;
        this.cooldown = cooldown;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public int getCooldown() {
        return cooldown;
    }
    
    public String getFormattedCooldown() {
        int seconds = cooldown / 1000;
        return seconds + " second" + (seconds > 1 ? "s" : "");
    }
}
