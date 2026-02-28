package com.soloarise.plugin.managers;

import com.soloarise.plugin.SoloArisePlugin;
import com.soloarise.plugin.models.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

public class ScoreboardManager {
    
    private final SoloArisePlugin plugin;
    
    public ScoreboardManager(SoloArisePlugin plugin) {
        this.plugin = plugin;
    }
    
    public void updateScoreboard(Player player) {
        if (!plugin.getConfig().getBoolean("scoreboard.enabled", true)) return;
        
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = board.registerNewObjective("souls", "dummy", 
            plugin.getConfig().getString("scoreboard.title", "&5&lSoul Collection").replace("&", "§"));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        
        PlayerSoulData soulData = plugin.getSoulManager().getPlayerSouls().get(player.getUniqueId());
        
        if (soulData != null) {
            int lineNumber = 0;
            
            // Header
            objective.getScore("§8§m-------------------").setScore(15 - lineNumber++);
            
            // Soul counts by rank
            long normalCount = soulData.getSoulsByRank(SoulRank.NORMAL).size();
            long warriorCount = soulData.getSoulsByRank(SoulRank.WARRIOR).size();
            long eliteCount = soulData.getSoulsByRank(SoulRank.ELITE).size();
            long bossCount = soulData.getSoulsByRank(SoulRank.BOSS).size();
            long playerCount = soulData.getSoulsByRank(SoulRank.PLAYER).size();
            
            if (normalCount > 0) objective.getScore("§7● Normal: §f" + normalCount).setScore(14 - lineNumber++);
            if (warriorCount > 0) objective.getScore("§a● Warrior: §f" + warriorCount).setScore(13 - lineNumber++);
            if (eliteCount > 0) objective.getScore("§9● Elite: §f" + eliteCount).setScore(12 - lineNumber++);
            if (bossCount > 0) objective.getScore("§c● BOSS: §f" + bossCount).setScore(11 - lineNumber++);
            if (playerCount > 0) objective.getScore("§d● Player: §f" + playerCount).setScore(10 - lineNumber++);
            
            objective.getScore("§8§m-------------------").setScore(9 - lineNumber++);
            objective.getScore("§e● Total: §f" + soulData.getSouls().size() + "/" + plugin.getConfigManager().getMaxSoulsPerPlayer()).setScore(8 - lineNumber++);
            
            // Recent souls (last 3)
            objective.getScore("§8§m-------------------").setScore(7 - lineNumber++);
            objective.getScore("§6Recent Souls:").setScore(6 - lineNumber++);
            
            int count = 0;
            for (CapturedSoul soul : soulData.getSouls()) {
                if (count >= 3) break;
                objective.getScore(" §7- " + soul.getRank().getDisplayName() + " §f" + soul.getName()).setScore(5 - lineNumber++);
                count++;
            }
            
            objective.getScore("§8§m-------------------").setScore(4 - lineNumber);
        } else {
            objective.getScore("§cNo souls captured yet!").setScore(5);
        }
        
        player.setScoreboard(board);
    }
    
    public void updateAllScoreboards() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updateScoreboard(player);
        }
    }
}
