package com.soloarise.plugin.models;

public enum SoulRank {
    NORMAL("§7Normal", 1),
    WARRIOR("§aWarrior", 2),
    ELITE("§9Elite", 3),
    BOSS("§cBOSS", 5),
    PLAYER("§dPlayer", 4);
    
    private final String displayName;
    private final int powerLevel;
    
    SoulRank(String displayName, int powerLevel) {
        this.displayName = displayName;
        this.powerLevel = powerLevel;
    }
    
    public String getDisplayName() { return displayName; }
    public int getPowerLevel() { return powerLevel; }
}
