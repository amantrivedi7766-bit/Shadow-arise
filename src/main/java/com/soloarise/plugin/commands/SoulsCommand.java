package com.soloarise.plugin.commands;

import com.soloarise.plugin.SoloArisePlugin;
import com.soloarise.plugin.models.CapturedSoul;
import com.soloarise.plugin.models.PlayerSoulData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SoulsCommand implements CommandExecutor {
    
    private final SoloArisePlugin plugin;
    
    public SoulsCommand(SoloArisePlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }
        
        PlayerSoulData soulData = plugin.getSoulManager().getPlayerSouls().get(player.getUniqueId());
        
        if (soulData == null || soulData.getSouls().isEmpty()) {
            player.sendMessage("§cYou have no souls! Kill mobs and use §e/arise§c to capture them.");
            return true;
        }
        
        player.sendMessage("§8§m--------------------------------------------------");
        player.sendMessage("§5§l✦ YOUR SOULS §7(" + soulData.getSouls().size() + ") ✦");
        player.sendMessage("§8§m--------------------------------------------------");
        
        for (CapturedSoul soul : soulData.getSouls()) {
            String status = soul.isSummoned() ? "§a[SUMMONED]" : "§7[AVAILABLE]";
            String health = String.format("%.0f/%.0f", soul.getCurrentHealth(), soul.getMaxHealth());
            
            player.sendMessage(" §e" + soul.getFormattedName() + " §7- " + 
                soul.getRank().getDisplayName() + " §7- HP: " + health + " " + status);
            player.sendMessage("   §7Ability: §f" + soul.getAbility().getName());
        }
        
        player.sendMessage("§8§m--------------------------------------------------");
        player.sendMessage("§7Crouch §e5 times §7to summon souls!");
        
        return true;
    }
}
