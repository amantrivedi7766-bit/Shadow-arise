package com.soloarise.plugin.managers;

import com.soloarise.plugin.SoloArisePlugin;
import com.soloarise.plugin.models.*;
import java.sql.*;
import java.util.UUID;

public class DatabaseManager {
    
    private final SoloArisePlugin plugin;
    private Connection connection;
    
    public DatabaseManager(SoloArisePlugin plugin) {
        this.plugin = plugin;
        initializeDatabase();
        createTables();
    }
    
    private void initializeDatabase() {
        try {
            // SQLite connection
            String url = "jdbc:sqlite:" + plugin.getDataFolder() + "/solarise.db";
            connection = DriverManager.getConnection(url);
        } catch (SQLException e) {
            e.printStackTrace();
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
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "player_uuid VARCHAR(36), " +
            "soul_uuid VARCHAR(36), " +
            "soul_name VARCHAR(50), " +
            "soul_rank VARCHAR(20), " +
            "energy INT" +
            ")";
            
        executeUpdate(playerTable);
        executeUpdate(soulTable);
    }
    
    public void executeUpdate(String query) {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void savePlayer(ArisePlayer player) {
        String query = "REPLACE INTO arise_players (uuid, player_name, has_arise_power, task_id, task_start_time) VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
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
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
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
        // Implementation for saving all data
    }
    
    public Connection getConnection() {
        return connection;
    }
}
