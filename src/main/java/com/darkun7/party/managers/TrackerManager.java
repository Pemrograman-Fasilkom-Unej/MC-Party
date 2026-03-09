package com.darkun7.party.managers;

import com.darkun7.party.PartyPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TrackerManager {

    private final PartyPlugin plugin;
    // Map of Tracker (Player UUID) -> Target (Friend UUID)
    private final Map<UUID, UUID> activeTrackers;

    public TrackerManager(PartyPlugin plugin) {
        this.plugin = plugin;
        this.activeTrackers = new HashMap<>();
        startTrackingTask();
    }

    public void startTracking(UUID trackerId, UUID targetId) {
        activeTrackers.put(trackerId, targetId);
    }

    public void stopTracking(UUID trackerId) {
        activeTrackers.remove(trackerId);
    }

    public boolean isTracking(UUID trackerId) {
        return activeTrackers.containsKey(trackerId);
    }

    public UUID getTarget(UUID trackerId) {
        return activeTrackers.get(trackerId);
    }

    private void startTrackingTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (activeTrackers.isEmpty())
                    return;

                for (Map.Entry<UUID, UUID> entry : activeTrackers.entrySet()) {
                    Player tracker = Bukkit.getPlayer(entry.getKey());
                    Player target = Bukkit.getPlayer(entry.getValue());

                    // If either is offline or not in the same world, skip this tick
                    if (tracker == null || target == null || !tracker.isOnline() || !target.isOnline()) {
                        continue;
                    }
                    if (!tracker.getWorld().equals(target.getWorld())) {
                        continue;
                    }

                    // Vector math: Direction from Tracker to Target
                    Location origin = tracker.getLocation().add(0, 1.2, 0); // Chest height
                    Location destination = target.getLocation().add(0, 1.2, 0);

                    double distance = origin.distance(destination);
                    if (distance < 2.0) {
                        continue; // Already very close
                    }

                    Vector direction = destination.toVector().subtract(origin.toVector()).normalize();

                    // Spawn particles originating from the player leading towards the target
                    // Show a short line of 3 particles in front of the tracker
                    for (int i = 1; i <= 3; i++) {
                        Location particleLoc = origin.clone().add(direction.clone().multiply(i));
                        // Reddust allows colored particles, but happy villager is very obvious and
                        // magical
                        tracker.spawnParticle(Particle.HAPPY_VILLAGER, particleLoc, 1, 0, 0, 0, 0);
                    }
                }
            }
        }.runTaskTimer(plugin, 10L, 10L); // Run every 0.5 seconds (10 ticks)
    }
}
