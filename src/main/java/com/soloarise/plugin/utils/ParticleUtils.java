package com.soloarise.plugin.utils;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class ParticleUtils {
    
    public static void drawCircle(Location center, Particle particle, double radius, int points) {
        World world = center.getWorld();
        for (int i = 0; i < points; i++) {
            double angle = 2 * Math.PI * i / points;
            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);
            world.spawnParticle(particle, x, center.getY(), z, 1, 0, 0, 0, 0);
        }
    }
    
    public static void drawSpiral(Location center, Particle particle, double radius, double height, int points) {
        World world = center.getWorld();
        for (int i = 0; i < points; i++) {
            double progress = (double) i / points;
            double angle = progress * 4 * Math.PI;
            double x = center.getX() + radius * Math.cos(angle) * progress;
            double y = center.getY() + height * progress;
            double z = center.getZ() + radius * Math.sin(angle) * progress;
            world.spawnParticle(particle, x, y, z, 1, 0, 0, 0, 0);
        }
    }
    
    public static void drawLine(Location start, Location end, Particle particle, double spacing) {
        Vector direction = end.toVector().subtract(start.toVector());
        double length = direction.length();
        direction.normalize();
        
        for (double d = 0; d <= length; d += spacing) {
            Location point = start.clone().add(direction.clone().multiply(d));
            start.getWorld().spawnParticle(particle, point, 1, 0, 0, 0, 0);
        }
    }
    
    public static void drawSphere(Location center, Particle particle, double radius, int density) {
        World world = center.getWorld();
        for (int i = 0; i < density; i++) {
            double u = Math.random();
            double v = Math.random();
            double theta = 2 * Math.PI * u;
            double phi = Math.acos(2 * v - 1);
            
            double x = center.getX() + radius * Math.sin(phi) * Math.cos(theta);
            double y = center.getY() + radius * Math.sin(phi) * Math.sin(theta);
            double z = center.getZ() + radius * Math.cos(phi);
            
            world.spawnParticle(particle, x, y, z, 1, 0, 0, 0, 0);
        }
    }
}
