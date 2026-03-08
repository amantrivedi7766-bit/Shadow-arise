package com.soloarise.plugin.commands.tabcompleters;

import com.soloarise.plugin.SoloArisePlugin;
import com.soloarise.plugin.models.CapturedSoul;
import com.soloarise.plugin.models.PlayerSoulData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SummonTabCompleter implements TabCompleter {
    
    private final SoloArisePlugin plugin;
    
    public SummonTabCompleter(SoloArisePlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player)) {
            return new ArrayList<>();
        }
        
        if (args.length == 1) {
            PlayerSoulData soulData = plugin.getSoulManager().getPlayerSouls().get(player.getUniqueId());
            if (soulData != null) {
                return soulData.getSouls().stream()
                    .map(CapturedSoul::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
            }
        }
        
        return new ArrayList<>();
    }
}
