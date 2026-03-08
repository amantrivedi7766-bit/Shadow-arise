package com.soloarise.plugin.commands.admin;

import com.soloarise.plugin.SoloArisePlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class AdminChestCommand implements CommandExecutor {
    
    private final SoloArisePlugin plugin;
    
    public AdminChestCommand(SoloArisePlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }
        
        if (!player.hasPermission("solarise.admin")) {
            player.sendMessage("§cYou don't have permission!");
            return true;
        }
        
        openAdminMenu(player);
        return true;
    }
    
    private void openAdminMenu(Player player) {
        Inventory menu = Bukkit.createInventory(null, 27, "§4§l✦ ADMIN PANEL ✦");
        
        // Reload Config
        menu.setItem(10, createMenuItem(
            Material.REDSTONE,
            "§cReload Config",
            "§7Click to reload plugin config"
        ));
        
        // Give Soul
        menu.setItem(11, createMenuItem(
            Material.SOUL_SAND,
            "§5Give Soul",
            "§7Give a soul to a player",
            "§7Click to select player"
        ));
        
        // Remove Soul
        menu.setItem(12, createMenuItem(
            Material.SOUL_CAMPFIRE,
            "§cRemove Soul",
            "§7Remove a soul from player",
            "§7Click to select player"
        ));
        
        // Heal All Souls
        menu.setItem(13, createMenuItem(
            Material.NETHER_STAR,
            "§aHeal All Souls",
            "§7Heal all souls of all players"
        ));
        
        // List Players
        menu.setItem(14, createMenuItem(
            Material.PLAYER_HEAD,
            "§ePlayer List",
            "§7View all players with souls",
            "§7Click to view"
        ));
        
        // Toggle PvP
        menu.setItem(15, createMenuItem(
            Material.DIAMOND_SWORD,
            "§6Toggle Soul PvP",
            "§7Enable/disable soul PvP",
            "§7Currently: §aEnabled"
        ));
        
        // Clear All Souls
        menu.setItem(16, createMenuItem(
            Material.BARRIER,
            "§c§lClear All Souls",
            "§7WARNING: Clears ALL souls",
            "§7from ALL players"
        ));
        
        // View Stats
        menu.setItem(19, createMenuItem(
            Material.BOOK,
            "§bServer Stats",
            "§7View server statistics",
            "§7Click to view"
        ));
        
        // Teleport to Soul
        menu.setItem(20, createMenuItem(
            Material.ENDER_PEARL,
            "§dTeleport to Soul",
            "§7Teleport to a summoned soul",
            "§7Click to select"
        ));
        
        // Kill All Souls
        menu.setItem(21, createMenuItem(
            Material.SKELETON_SKULL,
            "§cKill All Souls",
            "§7Kill all summoned souls"
        ));
        
        // Set Spawn
        menu.setItem(22, createMenuItem(
            Material.BEACON,
            "§aSet Soul Spawn",
            "§7Set soul spawn location"
        ));
        
        // Reset Player
        menu.setItem(23, createMenuItem(
            Material.TOTEM_OF_UNDYING,
            "§6Reset Player",
            "§7Reset a player's soul data"
        ));
        
        // Close
        menu.setItem(26, createMenuItem(
            Material.BARRIER,
            "§cClose",
            "§7Click to close"
        ));
        
        player.openInventory(menu);
    }
    
    private ItemStack createMenuItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }
}
