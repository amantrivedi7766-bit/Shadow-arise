package com.soloarise.plugin.commands;

import com.soloarise.plugin.SoloArisePlugin;
import com.soloarise.plugin.models.CapturedSoul;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class SummonCommand implements CommandExecutor {
    
    private final SoloArisePlugin plugin;
    
    public SummonCommand(SoloArisePlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }
        
        if (args.length < 1) {
            player.sendMessage("§cUsage: /summon <number>");
            return true;
        }
        
        // Check if player has an active summon session
        List<CapturedSoul> souls = plugin.getSummonSessionManager().getSession(player);
        if (souls == null) {
            player.sendMessage("§cPlease shift + left-click 3 times first to select a soul!");
            return true;
        }
        
        try {
            int index = Integer.parseInt(args[0]) - 1;
            
            if (index < 0 || index >= souls.size()) {
                player.sendMessage("§cInvalid number! Please choose between 1 and " + souls.size());
                return true;
            }
            
            CapturedSoul selectedSoul = souls.get(index);
            
            // Check if soul is already summoned
            if (selectedSoul.isSummoned()) {
                player.sendMessage("§cThis soul is already summoned!");
                return true;
            }
            
            // Check if soul has health
            if (selectedSoul.getCurrentHealth() <= 0) {
                player.sendMessage("§cThis soul is dead! Use /healsoul to revive it.");
                return true;
            }
            
            // Summon the soul
            boolean success = plugin.getSoulManager().summonSoul(player, selectedSoul.getOriginalId());
            
            if (success) {
                player.sendMessage("§a✓ Successfully summoned " + selectedSoul.getRank().getDisplayName() + 
                    " §f" + selectedSoul.getName());
                
                // Clear the session
                plugin.getSummonSessionManager().endSession(player);
            }
            
        } catch (NumberFormatException e) {
            player.sendMessage("§cPlease enter a valid number!");
        }
        
        return true;
    }
}
