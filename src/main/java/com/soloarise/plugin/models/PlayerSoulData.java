package com.soloarise.plugin.models;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerSoulData {
    
    private final UUID playerId;
    private final Map<UUID, CapturedSoul> souls = new ConcurrentHashMap<>();
    private static final int MAX_SOULS = 10000;
    
    public PlayerSoulData(UUID playerId) {
        this.playerId = playerId;
    }
    
    public boolean hasSoul(UUID soulId) {
        return souls.containsKey(soulId);
    }
    
    public void addSoul(CapturedSoul soul) {
        if (souls.size() < MAX_SOULS) {
            souls.put(soul.getOriginalId(), soul);
        }
    }
    
    public CapturedSoul getSoulById(UUID soulId) {
        return souls.get(soulId);
    }
    
    public CapturedSoul getSoulByName(String name) {
        return souls.values().stream()
            .filter(s -> s.getName().equalsIgnoreCase(name))
            .findFirst()
            .orElse(null);
    }
    
    public List<CapturedSoul> getSoulsByGroup(String groupName) {
        return souls.values().stream()
            .filter(s -> s.getGroupName().equalsIgnoreCase(groupName))
            .toList();
    }
    
    public List<CapturedSoul> getSoulsByRank(SoulRank rank) {
        return souls.values().stream()
            .filter(s -> s.getRank() == rank)
            .toList();
    }
    
    public List<CapturedSoul> getSummonedSouls() {
        return souls.values().stream()
            .filter(CapturedSoul::isSummoned)
            .toList();
    }
    
    public Collection<CapturedSoul> getSouls() {
        return souls.values();
    }
}
