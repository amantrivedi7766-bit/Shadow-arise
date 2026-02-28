package com.soloarise.plugin.models;

import org.bukkit.entity.Player;

public interface TaskCompletionChecker {
    boolean isComplete(Player player, ArisePlayer arisePlayer);
}
