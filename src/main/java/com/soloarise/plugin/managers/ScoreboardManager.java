package com.soloarise.plugin.managers;

import com.soloarise.plugin.SoloArisePlugin;
import com.soloarise.plugin.models.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

public class ScoreboardManager {
    
    private final SoloArisePlugin plugin;
    private final Map<UUID, Scoreboard> playerScoreboards = new ConcurrentHashMap<>();
    private final Map<UUID, Scoreboard> taskScoreboards = new ConcurrentHashMap<>();
    
    public ScoreboardManager(SoloArisePlugin plugin) {
        this.plugin = plugin;
    }
    
    public void updateMainScoreboard(Player player) {
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = board.registerNewObjective("souls", "dummy", 
            "§5§l✦ SOUL COLLECTION ✦");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        
        PlayerSoulData soulData = plugin.getSoulManager().getPlayerSouls().get(player.getUniqueId());
        ArisePlayer arisePlayer = plugin.getPlayerManager().getPlayer(player);
        
        int lineNumber = 0;
        
        // Header
        objective.getScore("§8§m--------------------").setScore(15 - lineNumber++);
        
        if (soulData != null) {
            // Soul counts by rank
            long normalCount = soulData.getSoulsByRank(SoulRank.NORMAL).size();
            long warriorCount = soulData.getSoulsByRank(SoulRank.WARRIOR).size();
            long eliteCount = soulData.getSoulsByRank(SoulRank.ELITE).size();
            long bossCount = soulData.getSoulsByRank(SoulRank.BOSS).size();
            long playerCount = soulData.getSoulsByRank(SoulRank.PLAYER).size();
            
            // Summoned souls count
            long summonedCount = soulData.getSummonedSouls().size();
            
            objective.getScore("§7Normal: §f" + normalCount).setScore(14 - lineNumber++);
            objective.getScore("§aWarrior: §f" + warriorCount).setScore(13 - lineNumber++);
            objective.getScore("§9Elite: §f" + eliteCount).setScore(12 - lineNumber++);
            objective.getScore("§cBOSS: §f" + bossCount).setScore(11 - lineNumber++);
            objective.getScore("§dPlayer: §f" + playerCount).setScore(10 - lineNumber++);
            
            objective.getScore("§8§m--------------------").setScore(9 - lineNumber++);
            objective.getScore("§eTotal: §f" + soulData.getSouls().size()).setScore(8 - lineNumber++);
            objective.getScore("§dSummoned: §f" + summonedCount).setScore(7 - lineNumber++);
            
            // Active souls status
            if (summonedCount > 0) {
                objective.getScore("§8§m--------------------").setScore(6 - lineNumber++);
                objective.getScore("§c⚡ ACTIVE SOULS:").setScore(5 - lineNumber++);
                
                int soulLine = 4;
                for (CapturedSoul soul : soulData.getSummonedSouls()) {
                    if (soulLine > 0) {
                        String healthBar = getHealthBar(soul.getCurrentHealth(), soul.getMaxHealth());
                        objective.getScore(" §7- " + soul.getRank().getDisplayName() + 
                            " §f" + soul.getName().substring(0, Math.min(8, soul.getName().length())) + 
                            " " + healthBar).setScore(soulLine--);
                    }
                }
            }
        } else {
            objective.getScore("§cNo souls captured!").setScore(5);
        }
        
        objective.getScore("§8§m--------------------").setScore(0);
        
        playerScoreboards.put(player.getUniqueId(), board);
        player.setScoreboard(board);
    }
    
    public void showTaskScoreboard(Player player) {
        ArisePlayer arisePlayer = plugin.getPlayerManager().getPlayer(player);
        if (!arisePlayer.hasActiveTask()) return;
        
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = board.registerNewObjective("task", "dummy", 
            "§6§l✦ CURRENT TASK ✦");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        
        Task task = arisePlayer.getCurrentTask();
        long elapsed = System.currentTimeMillis() - arisePlayer.getTaskStartTime();
        long remaining = Math.max(0, 3600000 - elapsed);
        
        int minutes = (int) (remaining / 60000);
        int seconds = (int) ((remaining % 60000) / 1000);
        
        double progress = Math.min(1.0, elapsed / 3600000.0);
        String progressBar = getProgressBar(progress);
        
        objective.getScore("§8§m--------------------").setScore(7);
        objective.getScore("§eTask: §f" + task.getName()).setScore(6);
        objective.getScore("§8§m--------------------").setScore(5);
        objective.getScore("§aProgress:").setScore(4);
        objective.getScore(progressBar).setScore(3);
        objective.getScore("§f" + (int)(progress * 100) + "%").setScore(2);
        objective.getScore("§8§m--------------------").setScore(1);
        objective.getScore("§cTime: §f" + String.format("%02d:%02d", minutes, seconds)).setScore(0);
        
        taskScoreboards.put(player.getUniqueId(), board);
        player.setScoreboard(board);
    }
    
    public void removeTaskScoreboard(Player player) {
        taskScoreboards.remove(player.getUniqueId());
        updateMainScoreboard(player);
    }
    
    private String getProgressBar(double progress) {
        int bars = 20;
        int completed = (int) (progress * bars);
        
        StringBuilder bar = new StringBuilder("§a");
        for (int i = 0; i < completed; i++) bar.append("■");
        bar.append("§7");
        for (int i = completed; i < bars; i++) bar.append("■");
        
        return bar.toString();
    }
    
    private String getHealthBar(double current, double max) {
        int bars = 10;
        int healthBars = (int) ((current / max) * bars);
        
        StringBuilder bar = new StringBuilder("§c");
        for (int i = 0; i < healthBars; i++) bar.append("❤");
        bar.append("§8");
        for (int i = healthBars; i < bars; i++) bar.append("❤");
        
        return bar.toString();
    }
    
    public void updateAllScoreboards() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updateMainScoreboard(player);
            
            ArisePlayer arisePlayer = plugin.getPlayerManager().getPlayer(player);
            if (arisePlayer.hasActiveTask()) {
                showTaskScoreboard(player);
            }
        }
    }
}
