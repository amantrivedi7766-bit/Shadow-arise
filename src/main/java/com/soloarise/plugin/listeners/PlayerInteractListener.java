package com.soloarise.plugin.listeners;  // Correct package

import com.soloarise.plugin.SoloArisePlugin;
import com.soloarise.plugin.models.CapturedSoul;
import com.soloarise.plugin.models.PlayerSoulData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class PlayerInteractListener implements Listener {
    
    private final SoloArisePlugin plugin;
    private final Map<UUID, ClickData> clickTracker = new HashMap<>();
    
    public PlayerInteractListener(SoloArisePlugin plugin) {
        this.plugin = plugin;
        
        new BukkitRunnable() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                clickTracker.entrySet().removeIf(entry -> 
                    currentTime - entry.getValue().getLastClickTime() > 3000);
            }
        }.runTaskTimer(plugin, 1200L, 1200L);
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        
        if (!player.isSneaking()) {
            return;
        }
        
        if (event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }
        
        if (!plugin.getPlayerManager().getPlayer(player).hasArisePower()) {
            return;
        }
        
        UUID playerId = player.getUniqueId();
        ClickData clickData = clickTracker.getOrDefault(playerId, new ClickData());
        
        clickData.addClick();
        clickTracker.put(playerId, clickData);
        
        if (clickData.getClickCount() >= 3) {
            if (clickData.isValid()) {
                openSummonMenu(player);
            }
            clickTracker.remove(playerId);
        }
    }
    
    private void openSummonMenu(Player player) {
        PlayerSoulData soulData = plugin.getSoulManager().getPlayerSouls().get(player.getUniqueId());
        
        if (soulData == null || soulData.getSouls().isEmpty()) {
            player.sendMessage("§cYou have no souls to summon!");
            return;
        }
        
        player.sendMessage("§8§m--------------------------------------------------");
        player.sendMessage("§5§l✦ SELECT SOUL TO SUMMON ✦");
        player.sendMessage("§8§m--------------------------------------------------");
        
        int index = 1;
        List<CapturedSoul> soulsList = new ArrayList<>(soulData.getSouls()); // Convert to List
        
        for (CapturedSoul soul : soulsList) {
            if (soul.isSummoned()) {
                player.sendMessage("§7" + index + ". " + getRankColor(soul) + " §f" + soul.getName() + 
                    " §c[ALREADY SUMMONED]");
            } else if (soul.getCurrentHealth() <= 0) {
                player.sendMessage("§7" + index + ". " + getRankColor(soul) + " §f" + soul.getName() + 
                    " §c[DEAD - NEEDS HEALING]");
            } else {
                player.sendMessage("§e" + index + ". " + getRankColor(soul) + " §f" + soul.getName() + 
                    " §7(Health: " + (int)soul.getCurrentHealth() + "/" + (int)soul.getMaxHealth() + ")");
            }
            index++;
        }
        
        player.sendMessage("§8§m--------------------------------------------------");
        player.sendMessage("§7Type §e/summon <number> §7to summon a soul");
        
        // Store souls list in session
        plugin.getSummonSessionManager().startSession(player, soulsList);
    }
    
    private String getRankColor(CapturedSoul soul) {
        return switch (soul.getRank().name()) {
            case "NORMAL" -> "§7";
            case "WARRIOR" -> "§a";
            case "ELITE" -> "§9";
            case "BOSS" -> "§c";
            case "PLAYER" -> "§d";
            default -> "§f";
        };
    }
    
    private static class ClickData {
        private int clickCount = 0;
        private long firstClickTime = 0;
        private long lastClickTime = 0;
        
        public void addClick() {
            long currentTime = System.currentTimeMillis();
            if (clickCount == 0) {
                firstClickTime = currentTime;
            }
            clickCount++;
            lastClickTime = currentTime;
        }
        
        public int getClickCount() { return clickCount; }
        public long getLastClickTime() { return lastClickTime; }
        
        public boolean isValid() {
            return (lastClickTime - firstClickTime) <= 3000;
        }
    }
}
