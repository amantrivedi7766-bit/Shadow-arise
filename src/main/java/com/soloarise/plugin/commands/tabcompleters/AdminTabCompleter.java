package com.soloarise.plugin.commands.tabcompleters;

import com.soloarise.plugin.SoloArisePlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AdminTabCompleter implements TabCompleter {
    
    private final SoloArisePlugin plugin;  // Add field
    private final List<String> subCommands = Arrays.asList(
        "reload", "givepower", "removesoul", "resettask", "view", "healall", "broadcast"
    );
    
    // Add constructor with plugin parameter
    public AdminTabCompleter(SoloArisePlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("solarise.admin")) {
            return new ArrayList<>();
        }
        
        if (args.length == 1) {
            return subCommands.stream()
                .filter(cmd -> cmd.startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
        }
        
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("givepower") || 
                args[0].equalsIgnoreCase("removesoul") || 
                args[0].equalsIgnoreCase("resettask") || 
                args[0].equalsIgnoreCase("view")) {
                
                return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
            }
        }
        
        return new ArrayList<>();
    }
}
