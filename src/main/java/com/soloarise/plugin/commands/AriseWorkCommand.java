package com.soloarise.plugin.commands;

import com.soloarise.plugin.SoloArisePlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AriseWorkCommand implements CommandExecutor {
    
    private final SoloArisePlugin plugin;
    
    public AriseWorkCommand(SoloArisePlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }
        
        if (args.length < 2) {
            player.sendMessage("§cUsage: /arisework <soulname> <workorder>");
            return true;
        }
        
        String soulName = args[0];
        String workOrder = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
        
        plugin.getSoulManager().orderWork(player, soulName, workOrder);
        return true;
    }
}
