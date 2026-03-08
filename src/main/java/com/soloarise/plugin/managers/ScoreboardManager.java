package com.soloarise.plugin.managers;

import com.soloarise.plugin.SoloArisePlugin;
import com.soloarise.plugin.models.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ScoreboardManager {
    
    private final SoloArisePlugin plugin;
    private final Map<UUID, Scoreboard> playerScoreboards = new ConcurrentHashMap<>();
    
    // Animation for scoreboard updates
    private int animationTick = 0;
    
    public ScoreboardManager(SoloArisePlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Update main scoreboard for a player
     */
    public void updateMainScoreboard(Player player) {
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = board.registerNewObjective("souls", "dummy", 
            getAnimatedTitle());
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        
        PlayerSoulData soulData = plugin.getSoulManager().getPlayerSouls().get(player.getUniqueId());
        
        int lineNumber = 15; // Start from top (increased for more lines)
        
        // Header
        objective.getScore("§8§m--------------------").setScore(lineNumber--);
        
        if (soulData != null && !soulData.getSouls().isEmpty()) {
            // Player info
            objective.getScore("§7Player: §f" + player.getName()).setScore(lineNumber--);
            objective.getScore("§8§m--------------------").setScore(lineNumber--);
            
            // Soul counts by rank
            Map<SoulRank, Long> rankCounts = soulData.getSoulCountByRank();
            
            long normalCount = rankCounts.getOrDefault(SoulRank.NORMAL, 0L);
            long warriorCount = rankCounts.getOrDefault(SoulRank.WARRIOR, 0L);
            long eliteCount = rankCounts.getOrDefault(SoulRank.ELITE, 0L);
            long bossCount = rankCounts.getOrDefault(SoulRank.BOSS, 0L);
            long playerCount = rankCounts.getOrDefault(SoulRank.PLAYER, 0L);
            
            // Show rank counts (only if > 0)
            if (normalCount > 0) 
                objective.getScore("§7Normal: §f" + normalCount).setScore(lineNumber--);
            if (warriorCount > 0) 
                objective.getScore("§aWarrior: §f" + warriorCount).setScore(lineNumber--);
            if (eliteCount > 0) 
                objective.getScore("§9Elite: §f" + eliteCount).setScore(lineNumber--);
            if (bossCount > 0) 
                objective.getScore("§cBOSS: §f" + bossCount).setScore(lineNumber--);
            if (playerCount > 0) 
                objective.getScore("§dPlayer: §f" + playerCount).setScore(lineNumber--);
            
            objective.getScore("§8§m--------------------").setScore(lineNumber--);
            
            // Total souls
            objective.getScore("§eTotal Souls: §f" + soulData.getSouls().size()).setScore(lineNumber--);
            
            // Active summoned souls
            int activeSouls = soulData.getActiveSummonCount();
            String activeStatus = activeSouls > 0 ? "§a✔" : "§7✘";
            objective.getScore("§dActive: §f" + activeSouls + " " + activeStatus).setScore(lineNumber--);
            
            // Show hunger multiplier if souls are active
            if (activeSouls > 0) {
                int hungerRate = activeSouls; // 1 point per soul
                String hungerWarning = activeSouls > 2 ? "§c⚠" : "§e⚠";
                objective.getScore("§6Hunger: §f" + hungerRate + "x " + hungerWarning).setScore(lineNumber--);
            }
            
            // Show active souls list (max 5)
            if (activeSouls > 0) {
                objective.getScore("§8§m--------------------").setScore(lineNumber--);
                objective.getScore("§c⚡ ACTIVE SOULS:").setScore(lineNumber--);
                
                int count = 0;
                for (CapturedSoul soul : soulData.getSummonedSouls()) {
                    if (count >= 5) break; // Limit to 5 lines
                    
                    String name = soul.getFormattedName();
                    if (name.length() > 10) {
                        name = name.substring(0, 10) + ".";
                    }
                    
                    String healthBar = soul.getCompactHealthBar();
                    String rankColor = getRankColor(soul.getRank());
                    
                    objective.getScore(" §7- " + rankColor + name + " " + healthBar).setScore(lineNumber--);
                    count++;
                }
                
                if (activeSouls > 5) {
                    objective.getScore(" §7... and " + (activeSouls - 5) + " more").setScore(lineNumber--);
                }
            }
            
        } else {
            // No souls message
            objective.getScore("§7No souls yet!").setScore(lineNumber--);
            objective.getScore("§7Kill mobs and use").setScore(lineNumber--);
            objective.getScore("§e/arise §7to capture!").setScore(lineNumber--);
            objective.getScore("§8§m--------------------").setScore(lineNumber--);
            objective.getScore("§7Crouch §e5 times").setScore(lineNumber--);
            objective.getScore("§7to summon menu").setScore(lineNumber--);
        }
        
        // Server info at bottom
        objective.getScore("§8§m--------------------").setScore(lineNumber--);
        objective.getScore("§7Online: §a" + Bukkit.getOnlinePlayers().size()).setScore(lineNumber--);
        objective.getScore("§8§m--------------------").setScore(0);
        
        playerScoreboards.put(player.getUniqueId(), board);
        player.setScoreboard(board);
    }
    
    /**
     * Get colored rank name
     */
    private String getRankColor(SoulRank rank) {
        return switch(rank) {
            case NORMAL -> "§7";
            case WARRIOR -> "§a";
            case ELITE -> "§9";
            case BOSS -> "§c";
            case PLAYER -> "§d";
        };
    }
    
    /**
     * Get animated title (cycles through colors)
     */
    private String getAnimatedTitle() {
        animationTick = (animationTick + 1) % 20;
        
        String color;
        if (animationTick < 5) color = "§5";
        else if (animationTick < 10) color = "§d";
        else if (animationTick < 15) color = "§5";
        else color = "§d";
        
        return color + "✦ §lSOLO ARISE §r" + color + "✦";
    }
    
    /**
     * Show summon selection menu (temporary scoreboard)
     */
    public void showSummonMenu(Player player, List<CapturedSoul> souls) {
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = board.registerNewObjective("summon", "dummy", 
            "§5§l✦ SELECT SOUL ✦");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        
        objective.getScore("§8§m--------------------").setScore(9);
        objective.getScore("§7Crouch: §e5 times").setScore(8);
        objective.getScore("§7to open menu").setScore(7);
        objective.getScore("§8§m--------------------").setScore(6);
        
        int slot = 5;
        int index = 1;
        for (CapturedSoul soul : souls) {
            if (slot < 1) break;
            
            String status = soul.isSummoned() ? "§c[ACTIVE]" : 
                           (soul.isDead() ? "§c[DEAD]" : "§a[READY]");
            
            String name = soul.getFormattedName();
            if (name.length() > 12) {
                name = name.substring(0, 12) + ".";
            }
            
            objective.getScore("§e" + index + ". " + getRankColor(soul.getRank()) + 
                name + " " + status).setScore(slot--);
            index++;
        }
        
        objective.getScore("§8§m--------------------").setScore(0);
        
        player.setScoreboard(board);
        
        // Revert to main scoreboard after 10 seconds
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                updateMainScoreboard(player);
            }
        }, 200L);
    }
    
    /**
     * Show soul info (when using /souls command)
     */
    public void showSoulInfo(Player player, CapturedSoul soul) {
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = board.registerNewObjective("soulinfo", "dummy", 
            soul.getColoredName());
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        
        int line = 10;
        
        objective.getScore("§8§m--------------------").setScore(line--);
        objective.getScore("§7Rank: " + soul.getRank().getDisplayName()).setScore(line--);
        objective.getScore("§7Health: " + getDetailedHealthBar(soul)).setScore(line--);
        objective.getScore("§7  §f" + (int)soul.getCurrentHealth() + "/" + 
            (int)soul.getMaxHealth() + " HP").setScore(line--);
        objective.getScore("§8§m--------------------").setScore(line--);
        objective.getScore("§6Ability: §f" + soul.getAbility().getName()).setScore(line--);
        objective.getScore("§7" + soul.getAbility().getDescription()).setScore(line--);
        objective.getScore("§8§m--------------------").setScore(line--);
        
        String status;
        if (soul.isSummoned()) {
            status = "§aSUMMONED";
        } else if (soul.isDead()) {
            status = "§cDEAD";
        } else {
            status = "§eAVAILABLE";
        }
        objective.getScore("§7Status: " + status).setScore(line--);
        
        if (!soul.isSummoned() && !soul.isDead()) {
            objective.getScore("§7Summon: §e/crouch 5x").setScore(line--);
        } else if (soul.isDead()) {
            objective.getScore("§7Heal: §e/soulheal").setScore(line--);
        }
        
        objective.getScore("§8§m--------------------").setScore(0);
        
        player.setScoreboard(board);
        
        // Revert after 8 seconds
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                updateMainScoreboard(player);
            }
        }, 160L);
    }
    
    /**
     * Show death message (when soul dies)
     */
    public void showSoulDeath(Player player, CapturedSoul soul) {
        player.sendMessage("§c§l❌ SOUL LOST! ❌");
        player.sendMessage("§7Your " + soul.getColoredName() + " §7has died!");
        player.sendMessage("§7Use §e/soulheal " + soul.getFormattedName() + 
            " §7with diamonds to revive it.");
        
        // Update scoreboard to show death
        updateMainScoreboard(player);
    }
    
    /**
     * Get detailed health bar with numbers
     */
    private String getDetailedHealthBar(CapturedSoul soul) {
        int bars = 20;
        int healthBars = (int) ((soul.getCurrentHealth() / soul.getMaxHealth()) * bars);
        
        StringBuilder bar = new StringBuilder("§c");
        for (int i = 0; i < healthBars; i++) bar.append("|");
        bar.append("§8");
        for (int i = healthBars; i < bars; i++) bar.append("|");
        
        return bar.toString();
    }
    
    /**
     * Show capture attempt result
     */
    public void showCaptureResult(Player player, boolean success, String mobName) {
        if (success) {
            player.sendMessage("§a§l✦ SOUL CAPTURED! ✦");
            player.sendMessage("§7You now have a §f" + mobName + "§7 soul!");
        } else {
            player.sendMessage("§c§l❌ CAPTURE FAILED! ❌");
            player.sendMessage("§7The soul has been lost forever!");
        }
        
        // Update scoreboard
        updateMainScoreboard(player);
    }
    
    /**
     * Update all online players' scoreboards
     */
    public void updateAllScoreboards() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updateMainScoreboard(player);
        }
    }
    
    /**
     * Remove player's scoreboard
     */
    public void removeScoreboard(Player player) {
        playerScoreboards.remove(player.getUniqueId());
        player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
    }
    
    /**
     * Show hunger warning when low
     */
    public void showHungerWarning(Player player) {
        PlayerSoulData soulData = plugin.getSoulManager().getPlayerSouls().get(player.getUniqueId());
        if (soulData != null && soulData.getActiveSummonCount() > 0) {
            player.sendMessage("§c⚠ Your hunger is running low!");
            player.sendMessage("§7Souls drain hunger faster. Use §e/soulcome §7to recall them.");
        }
    }
    
    /**
     * Show recall message
     */
    public void showRecallMessage(Player player, int count) {
        if (count > 0) {
            player.sendMessage("§d✦ Recalled " + count + " soul" + (count > 1 ? "s" : "") + "!");
        }
        updateMainScoreboard(player);
    }
                           }
