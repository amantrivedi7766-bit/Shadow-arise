package com.soloarise.plugin.commands;

import com.soloarise.plugin.SoloArisePlugin;
import com.soloarise.plugin.models.CapturedSoul;
import com.soloarise.plugin.models.PlayerSoulData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
            player.sendMessage("§cUsage: /summon <soulname>");
            return true;
        }
        
        String soulName = args[0];
        PlayerSoulData soulData = plugin.getSoulManager().getPlayerSouls().get(player.getUniqueId());
        
        if (soulData == null) {
            player.sendMessage("§cYou have no souls!");
            return true;
        }
        
        CapturedSoul soul = soulData.getSoulByName(soulName);
        if (soul == null) {
            player.sendMessage("§cSoul not found! Use §e/souls §cto see your souls.");
            return true;
        }
        
        // Summon the soul
        boolean success = plugin.getSoulManager().summonSoul(player, soul);
        
        if (success) {
            player.sendMessage("§a✓ Successfully summoned " + soul.getColoredName());
        }
        
        return true;
    }
}
