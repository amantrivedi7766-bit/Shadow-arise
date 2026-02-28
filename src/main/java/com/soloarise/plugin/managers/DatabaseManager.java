package com.soloarise.plugin.managers;

import com.soloarise.plugin.SoloArisePlugin;
import com.soloarise.plugin.database.Database;
import com.soloarise.plugin.database.SQLiteDatabase;
import com.soloarise.plugin.database.MySQLDatabase;
import com.soloarise.plugin.models.*;
import java.sql.*;
import java.util.UUID;

public class DatabaseManager {
    
    private final SoloArisePlugin plugin;
    private Database database;
    
    public DatabaseManager(SoloArisePlugin plugin) {
        this.plugin = plugin;
        initializeDatabase();
        createTables();
    }
    
    private void initializeDatabase() {
        String dbType = plugin.getConfig().getString("database.type", "sqlite");
        
        if (dbType.equalsIgnoreCase("mysql")) {
            database = new MySQLDatabase(plugin);
        } else {
            database = new SQLiteDatabase(plugin);
        }
    }
    
    private void createTables() {
        String playerTable = "CREATE TABLE IF NOT EXISTS arise_players (" +
            "uuid VARCHAR(36) PRIMARY KEY, " +
            "player_name VARCHAR(16), " +
            "has_arise_power BOOLEAN, " +
            "task_id INT, " +
            "task_start_time LONG" +
            ")";
            
        String soulTable = "CREATE TABLE IF NOT EXISTS captured_souls (" +
            "id INT AUTO_INCREMENT PRIMARY KEY, " +
            "player_uuid VARCHAR(36), " +
            "soul_uuid VARCHAR(36), " +
            "soul_name VARCHAR(50), " +
            "soul_rank VARCHAR(20), " +
            "energy INT" +
            ")";
            
        database.executeUpdate(playerTable);
        database.executeUpdate(soulTable);
    }
    
    public void savePlayer(ArisePlayer player) {
        String query = "REPLACE INTO arise_players (uuid, player_name, has_arise_power, task_id, task_start_time) VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = database.getConnection().prepareStatement(query)) {
            stmt.setString(1, player.getPlayerId().toString());
            stmt.setString(2, player.getPlayerName());
            stmt.setBoolean(3, player.hasArisePower());
            stmt.setInt(4, player.getCurrentTask() != null ? player.getCurrentTask().getId() : -1);
            stmt.setLong(5, player.getTaskStartTime());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void saveSoul(UUID playerUuid, CapturedSoul soul) {
        String query = "INSERT INTO captured_souls (player_uuid, soul_uuid, soul_name, soul_rank, energy) VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = database.getConnection().prepareStatement(query)) {
            stmt.setString(1, playerUuid.toString());
            stmt.setString(2, soul.getOriginalId().toString());
            stmt.setString(3, soul.getName());
            stmt.setString(4, soul.getRank().name());
            stmt.setInt(5, soul.getEnergy());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void saveAll() {
        // Save all player data
        for (var entry : plugin.getPlayerManager().getAllPlayers().entrySet()) {
            savePlayer(entry.getValue());
        }
        
        // Save all soul data
        for (var entry : plugin.getSoulManager().getAllSoulData().entrySet()) {
            for (CapturedSoul soul : entry.getValue().getSouls()) {
                saveSoul(entry.getKey(), soul);
            }
        }
    }
}
