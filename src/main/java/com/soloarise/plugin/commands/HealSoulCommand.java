package com.soloarise.plugin.commands;

import com.soloarise.plugin.SoloArisePlugin;
import com.soloarise.plugin.models.CapturedSoul;
import com.soloarise.plugin.models.PlayerSoulData;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.buk.command.CommandSender;
import org.bukkit.inventory.ItemStack;

public class HealSoulCommand implements CommandExecutor {
    
    private final SoloArisePlugin plugin;
    
    public HealSoulCommand(SoloArisePlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }
        
        if (args.length < 1) {
            player.sendMessage("§cUsage: /healsoul <soulname>");
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
            player.sendMessage("§cSoul not found!");
            return true;
        }
        
        // Check if player has diamonds for healing
        int diamondsNeeded = (int) ((soul.getMaxHealth() - soul.getCurrentHealth()) / 10);
        if (diamondsNeeded < 1) diamondsNeeded = 1;
        
        if (!player.getInventory().contains(Material.DIAMOND, diamondsNeeded)) {
            player.sendMessage("§cYou need " + diamondsNeeded + " diamonds to heal this soul!");
            return true;
        }
        
        // Remove diamonds
        player.getInventory().removeItem(new ItemStack(Material.DIAMOND, diamondsNeeded));
        
        // Heal soul
        soul.setCurrentHealth(soul.getMaxHealth());
        
        player.sendMessage("§a✓ Soul " + soul.getName() + " has been fully healed!");
        plugin.getScoreboardManager().updateMainScoreboard(player);
        
        return true;
    }
}
