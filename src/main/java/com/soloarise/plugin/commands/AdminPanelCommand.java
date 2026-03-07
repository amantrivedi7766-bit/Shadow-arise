package com.soloarise.plugin.commands.admin;

import com.soloarise.plugin.SoloArisePlugin;
import com.soloarise.plugin.models.*;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AdminPanelCommand implements CommandExecutor {
    
    private final SoloArisePlugin plugin;
    
    public AdminPanelCommand(SoloArisePlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("solarise.admin")) {
            sender.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }
        
        if (args.length == 0) {
            showAdminPanel(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "reload":
                plugin.getConfigManager().reload();
                sender.sendMessage("§a✓ Configuration reloaded!");
                break;
                
            case "givepower":
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /solarise givepower <player>");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target != null) {
                    plugin.getPlayerManager().getPlayer(target).setHasArisePower(true);
                    sender.sendMessage("§a✓ Given arise power to " + target.getName());
                } else {
                    sender.sendMessage("§cPlayer not found!");
                }
                break;
                
            case "removesoul":
                if (args.length < 3) {
                    sender.sendMessage("§cUsage: /solarise removesoul <player> <soulname>");
                    return true;
                }
                Player p = Bukkit.getPlayer(args[1]);
                if (p != null) {
                    boolean removed = plugin.getSoulManager().releaseSoul(p, args[2]);
                    if (removed) {
                        sender.sendMessage("§a✓ Removed soul from " + p.getName());
                    } else {
                        sender.sendMessage("§cSoul not found!");
                    }
                }
                break;
                
            case "resettask":
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /solarise resettask <player>");
                    return true;
                }
                Player taskPlayer = Bukkit.getPlayer(args[1]);
                if (taskPlayer != null) {
                    plugin.getPlayerManager().removeActiveTask(taskPlayer.getUniqueId());
                    plugin.getTaskManager().removeTaskBossBar(taskPlayer);
                    plugin.getPlayerManager().assignTask(taskPlayer);
                    sender.sendMessage("§a✓ Task reset for " + taskPlayer.getName());
                }
                break;
                
            case "view":
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /solarise view <player>");
                    return true;
                }
                Player viewPlayer = Bukkit.getPlayer(args[1]);
                if (viewPlayer != null) {
                    viewPlayerData(sender, viewPlayer);
                }
                break;
                
            case "healall":
                for (Player online : Bukkit.getOnlinePlayers()) {
                    PlayerSoulData data = plugin.getSoulManager().getPlayerSouls().get(online.getUniqueId());
                    if (data != null) {
                        for (CapturedSoul soul : data.getSouls()) {
                            soul.heal(1000);
                        }
                    }
                }
                sender.sendMessage("§a✓ All souls healed!");
                break;
                
            case "broadcast":
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /solarise broadcast <message>");
                    return true;
                }
                String message = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
                Bukkit.broadcastMessage("§5[Solarise] §f" + message);
                break;
                
            default:
                sender.sendMessage("§cUnknown subcommand!");
        }
        
        return true;
    }
    
    private void showAdminPanel(CommandSender sender) {
        sender.sendMessage("§8§m--------------------------------------------------");
        sender.sendMessage("§5§l✦ SOLARISE ADMIN PANEL ✦");
        sender.sendMessage("§8§m--------------------------------------------------");
        sender.sendMessage("§e/solarise reload §7- Reload config");
        sender.sendMessage("§e/solarise givepower <player> §7- Give arise power");
        sender.sendMessage("§e/solarise removesoul <player> <soul> §7- Remove soul");
        sender.sendMessage("§e/solarise resettask <player> §7- Reset player task");
        sender.sendMessage("§e/solarise view <player> §7- View player data");
        sender.sendMessage("§e/solarise healall §7- Heal all souls");
        sender.sendMessage("§e/solarise broadcast <msg> §7- Broadcast message");
        sender.sendMessage("§8§m--------------------------------------------------");
    }
    
    private void viewPlayerData(CommandSender sender, Player player) {
        ArisePlayer ap = plugin.getPlayerManager().getPlayer(player);
        PlayerSoulData sd = plugin.getSoulManager().getPlayerSouls().get(player.getUniqueId());
        
        sender.sendMessage("§8§m--------------------------------------------------");
        sender.sendMessage("§5§l✦ " + player.getName() + "'s Data ✦");
        sender.sendMessage("§8§m--------------------------------------------------");
        sender.sendMessage("§eArise Power: " + (ap.hasArisePower() ? "§a✓" : "§c✗"));
        sender.sendMessage("§eActive Task: " + (ap.hasActiveTask() ? ap.getCurrentTask().getName() : "§7None"));
        
        if (sd != null) {
            sender.sendMessage("§eTotal Souls: §f" + sd.getSouls().size());
            sender.sendMessage("§eNormal: §f" + sd.getSoulsByRank(SoulRank.NORMAL).size());
            sender.sendMessage("§aWarrior: §f" + sd.getSoulsByRank(SoulRank.WARRIOR).size());
            sender.sendMessage("§9Elite: §f" + sd.getSoulsByRank(SoulRank.ELITE).size());
            sender.sendMessage("§cBOSS: §f" + sd.getSoulsByRank(SoulRank.BOSS).size());
            sender.sendMessage("§dPlayer: §f" + sd.getSoulsByRank(SoulRank.PLAYER).size());
        }
        sender.sendMessage("§8§m--------------------------------------------------");
    }
}
