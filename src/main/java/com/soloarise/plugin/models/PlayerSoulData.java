package com.soloarise.plugin.models;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class PlayerSoulData {
    
    private final UUID playerId;
    private final Map<UUID, CapturedSoul> souls = new ConcurrentHashMap<>();
    private static final int MAX_SOULS = 10000;
    private int totalSoulsCaptured = 0;
    private int bossSoulsCaptured = 0;
    private long lastSummonTime = 0;
    
    public PlayerSoulData(UUID playerId) {
        this.playerId = playerId;
    }
    
    // Soul management
    public boolean hasSoul(UUID soulId) {
        return souls.containsKey(soulId);
    }
    
    public boolean hasSoulByName(String name) {
        return souls.values().stream()
            .anyMatch(s -> s.getName().equalsIgnoreCase(name));
    }
    
    public void addSoul(CapturedSoul soul) {
        if (souls.size() < MAX_SOULS) {
            souls.put(soul.getOriginalId(), soul);
            totalSoulsCaptured++;
            
            if (soul.getRank() == SoulRank.BOSS) {
                bossSoulsCaptured++;
            }
        }
    }
    
    public boolean removeSoul(UUID soulId) {
        CapturedSoul removed = souls.remove(soulId);
        if (removed != null && removed.getRank() == SoulRank.BOSS) {
            bossSoulsCaptured--;
        }
        return removed != null;
    }
    
    public boolean removeSoulByName(String name) {
        Optional<CapturedSoul> soul = souls.values().stream()
            .filter(s -> s.getName().equalsIgnoreCase(name))
            .findFirst();
        
        if (soul.isPresent()) {
            return removeSoul(soul.get().getOriginalId());
        }
        return false;
    }
    
    // Getters
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
            .collect(Collectors.toList());
    }
    
    public List<CapturedSoul> getSoulsByRank(SoulRank rank) {
        return souls.values().stream()
            .filter(s -> s.getRank() == rank)
            .collect(Collectors.toList());
    }
    
    public List<CapturedSoul> getSummonedSouls() {
        return souls.values().stream()
            .filter(CapturedSoul::isSummoned)
            .collect(Collectors.toList());
    }
    
    public Collection<CapturedSoul> getSouls() {
        return souls.values();
    }
    
    public int getSoulCount() {
        return souls.size();
    }
    
    public int getTotalSoulsCaptured() {
        return totalSoulsCaptured;
    }
    
    public int getBossSoulsCaptured() {
        return bossSoulsCaptured;
    }
    
    public long getLastSummonTime() {
        return lastSummonTime;
    }
    
    public void setLastSummonTime(long time) {
        this.lastSummonTime = time;
    }
    
    // Statistics
    public Map<SoulRank, Long> getSoulCountByRank() {
        return souls.values().stream()
            .collect(Collectors.groupingBy(
                CapturedSoul::getRank,
                Collectors.counting()
            ));
    }
    
    public boolean hasAnySummoned() {
        return souls.values().stream().anyMatch(CapturedSoul::isSummoned);
    }
    
    public int getActiveSummonCount() {
        return (int) souls.values().stream()
            .filter(CapturedSoul::isSummoned)
            .count();
    }
    
    public void recallAll() {
        for (CapturedSoul soul : getSummonedSouls()) {
            if (soul.getSummonedEntity() != null) {
                soul.getSummonedEntity().remove();
            }
            soul.setSummoned(false);
            soul.setSummonedEntity(null);
        }
    }
    
    public void healAll() {
        for (CapturedSoul soul : souls.values()) {
            soul.healFully();
        }
    }
    
    public boolean isEmpty() {
        return souls.isEmpty();
    }
    
    public void clear() {
        recallAll();
        souls.clear();
        totalSoulsCaptured = 0;
        bossSoulsCaptured = 0;
    }
}
