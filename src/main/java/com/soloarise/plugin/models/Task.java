package com.soloarise.plugin.models;

import org.bukkit.entity.Player;

public class Task {
    
    private final int id;
    private final String name;
    private final TaskCompletionChecker completionCondition;
    
    public Task(int id, String name, TaskCompletionChecker completionCondition) {
        this.id = id;
        this.name = name;
        this.completionCondition = completionCondition;
    }
    
    public int getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public boolean isComplete(Player player, ArisePlayer arisePlayer) {
        return completionCondition.isComplete(player, arisePlayer);
    }
    
    @Override
    public String toString() {
        return "Task{id=" + id + ", name='" + name + "'}";
    }
}
