package org.astral.parkour_plugin.parkour.progress;

import org.bukkit.Location;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ProgressTrackerManager {
    private static final Map<String, ProgressTracker> trackerByMap = new HashMap<>();

    public static ProgressTracker get(final String map) {
        return trackerByMap.computeIfAbsent(map, k -> new ProgressTracker());
    }

    public static void remove(final String map) {
        trackerByMap.remove(map);
    }

    public static double getRadialProgress(Location start, Location end, Location current) {
        if (start == null || end == null || current == null || !start.getWorld().equals(current.getWorld())) {
            return 0.0;
        }
        double maxDistance = start.distance(end);
        double currentDistance = current.distance(end);

        if (currentDistance >= maxDistance) return 0.0;

        double progress = 1.0 - (currentDistance / maxDistance);
        return Math.round(progress * 10000.0) / 100.0;
    }

    public static double getMaxRadialProgress(Location start, List<Location> endPoints, Location current) {
        if (start == null || current == null || endPoints == null || endPoints.isEmpty()) {
            return 0.0;
        }

        double maxProgress = 0.0;
        for (Location end : endPoints) {
            if (end.getWorld() != null && end.getWorld().equals(start.getWorld())) {
                double progress = getRadialProgress(start, end, current);
                if (progress > maxProgress) {
                    maxProgress = progress;
                }
            }
        }
        return maxProgress;
    }

}