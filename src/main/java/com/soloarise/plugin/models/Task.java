package com.soloarise.plugin.models;

import org.bukkit.entity.Player;
import java.util.function.Predicate;

public class Task {
    
    private final int id;
    private final String name;
    private final Predicate<Player> completionCondition;
    
    public Task(int id, String name, Predicate<Player> completionCondition) {
        this.id = id;
        this.name = name;
        this.completionCondition = completionCondition;
    }
    
    public int getId() { return id; }
    public String getName() { return name; }
    
    public boolean isComplete(Player player) {
        return completionCondition.test(player);
    }
}
