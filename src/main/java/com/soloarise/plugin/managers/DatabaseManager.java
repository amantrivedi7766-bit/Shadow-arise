package com.soloarise.plugin.managers;

import com.soloarise.plugin.SoloArisePlugin;
import com.soloarise.plugin.models.*;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DatabaseManager {
    
    private final SoloArisePlugin plugin;
    private final File dataFolder;
    private final Map<UUID, Map<String, Object>> playerDataCache = new ConcurrentHashMap<>();
    
    public DatabaseManager(SoloArisePlugin plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "playerdata");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }
    
    public void savePlayerData(Player player) {
        File playerFile = new File(dataFolder, player.getUniqueId().toString() + ".dat");
        
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(playerFile))) {
            Map<String, Object> data = new HashMap<>();
            
            // Save ArisePlayer data
            ArisePlayer arisePlayer = plugin.getPlayerManager().getPlayer(player);
            data.put("hasArisePower", arisePlayer.hasArisePower());
            data.put("mobsKilled", arisePlayer.getMobsKilled());
            data.put("blocksMined", arisePlayer.getBlocksMined());
            data.put("distanceTraveled", arisePlayer.getDistanceTraveled());
            
            // Save soul data
            PlayerSoulData soulData = plugin.getSoulManager().getPlayerSouls().get(player.getUniqueId());
            if (soulData != null) {
                List<Map<String, Object>> soulsList = new ArrayList<>();
                for (CapturedSoul soul : soulData.getSouls()) {
                    Map<String, Object> soulMap = new HashMap<>();
                    soulMap.put("originalId", soul.getOriginalId().toString());
                    soulMap.put("name", soul.getName());
                    soulMap.put("rank", soul.getRank().name());
                    soulMap.put("currentHealth", soul.getCurrentHealth());
                    soulMap.put("maxHealth", soul.getMaxHealth());
                    soulMap.put("energy", soul.getEnergy());
                    soulMap.put("groupName", soul.getGroupName());
                    soulMap.put("entityType", soul.getEntityType().name());
                    soulsList.add(soulMap);
                }
                data.put("souls", soulsList);
                data.put("totalSoulsCaptured", soulData.getTotalSoulsCaptured());
                data.put("bossSoulsCaptured", soulData.getBossSoulsCaptured());
            }
            
            oos.writeObject(data);
            
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save data for player " + player.getName());
            e.printStackTrace();
        }
    }
    
    public void loadPlayerData(Player player) {
        File playerFile = new File(dataFolder, player.getUniqueId().toString() + ".dat");
        
        if (!playerFile.exists()) {
            return;
        }
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(playerFile))) {
            Map<String, Object> data = (Map<String, Object>) ois.readObject();
            playerDataCache.put(player.getUniqueId(), data);
            
            // Load ArisePlayer data
            ArisePlayer arisePlayer = plugin.getPlayerManager().getPlayer(player);
            arisePlayer.setHasArisePower((boolean) data.getOrDefault("hasArisePower", false));
            
            // Convert UUID to int - FIXED
            Object mobsKilledObj = data.get("mobsKilled");
            if (mobsKilledObj instanceof Integer) {
                arisePlayer.setMobsKilled((int) mobsKilledObj);
            } else if (mobsKilledObj instanceof Long) {
                arisePlayer.setMobsKilled(((Long) mobsKilledObj).intValue());
            }
            
            Object blocksMinedObj = data.get("blocksMined");
            if (blocksMinedObj instanceof Integer) {
                arisePlayer.setBlocksMined((int) blocksMinedObj);
            } else if (blocksMinedObj instanceof Long) {
                arisePlayer.setBlocksMined(((Long) blocksMinedObj).intValue());
            }
            
            Object distanceTraveledObj = data.get("distanceTraveled");
            if (distanceTraveledObj instanceof Integer) {
                arisePlayer.setDistanceTraveled((int) distanceTraveledObj);
            } else if (distanceTraveledObj instanceof Long) {
                arisePlayer.setDistanceTraveled(((Long) distanceTraveledObj).intValue());
            }
            
            // Load soul data
            List<Map<String, Object>> soulsList = (List<Map<String, Object>>) data.get("souls");
            if (soulsList != null) {
                PlayerSoulData soulData = new PlayerSoulData(player.getUniqueId());
                for (Map<String, Object> soulMap : soulsList) {
                    UUID originalId = UUID.fromString((String) soulMap.get("originalId"));
                    String name = (String) soulMap.get("name");
                    SoulRank rank = SoulRank.valueOf((String) soulMap.get("rank"));
                    
                    CapturedSoul soul = new CapturedSoul(originalId, name, rank);
                    soul.setCurrentHealth((double) soulMap.get("currentHealth"));
                    
                    Object maxHealthObj = soulMap.get("maxHealth");
                    if (maxHealthObj instanceof Double) {
                        soul.setMaxHealth((double) maxHealthObj);
                    } else if (maxHealthObj instanceof Integer) {
                        soul.setMaxHealth(((Integer) maxHealthObj).doubleValue());
                    }
                    
                    soul.heal((int) soulMap.getOrDefault("energy", 100));
                    
                    soulData.addSoul(soul);
                }
                
                plugin.getSoulManager().getPlayerSouls().put(player.getUniqueId(), soulData);
            }
            
        } catch (IOException | ClassNotFoundException e) {
            plugin.getLogger().warning("Failed to load data for player " + player.getName());
            e.printStackTrace();
        }
    }
    
    public void saveAll() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            savePlayerData(player);
        }
    }
}
