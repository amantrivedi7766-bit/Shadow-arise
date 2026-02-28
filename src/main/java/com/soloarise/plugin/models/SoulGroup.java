package com.soloarise.plugin.models;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class SoulGroup {
    
    private final String name;
    private final List<CapturedSoul> souls = new CopyOnWriteArrayList<>();
    
    public SoulGroup(String name) {
        this.name = name.toLowerCase();
    }
    
    public String getName() { return name; }
    
    public void addSoul(CapturedSoul soul) {
        souls.add(soul);
    }
    
    public boolean removeSoul(CapturedSoul soul) {
        return souls.remove(soul);
    }
    
    public List<CapturedSoul> getSouls() {
        return new ArrayList<>(souls);
    }
    
    public int getSize() {
        return souls.size();
    }
    
    public boolean contains(String soulName) {
        return souls.stream().anyMatch(s -> s.getName().equalsIgnoreCase(soulName));
    }
    
    public int getTotalEnergy() {
        return souls.stream().mapToInt(CapturedSoul::getEnergy).sum();
    }
    
    public void healAll(int amount) {
        souls.forEach(s -> s.heal(amount));
    }
}
