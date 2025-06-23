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

    public static double getLinearProgressPercentage(Location start, Location end, Location current) {
        if (start == null || end == null || current == null || !start.getWorld().equals(current.getWorld())) {
            return 0.0;
        }
        double dx = end.getX() - start.getX();

        double dy = end.getY() - start.getY();
        double dz = end.getZ() - start.getZ();

        double px = current.getX() - start.getX();
        double py = current.getY() - start.getY();
        double pz = current.getZ() - start.getZ();

        double dotProduct = px * dx + py * dy + pz * dz;
        double totalDistanceSquared = dx * dx + dy * dy + dz * dz;

        double progressFraction = Math.max(0, Math.min(1, dotProduct / totalDistanceSquared));

        return progressFraction * 100.0;
    }

    public static double getNearestEndPointProgress(Location start, List<Location> endPoints, Location current) {
        if (start == null || current == null || endPoints == null || endPoints.isEmpty()) {
            return 0.0;
        }

        Location nearestEnd = null;
        double shortestDistance = Double.MAX_VALUE;

        for (Location end : endPoints) {
            if (end.getWorld() != null && end.getWorld().equals(start.getWorld())) {
                double dist = current.distance(end);
                if (dist < shortestDistance) {
                    shortestDistance = dist;
                    nearestEnd = end;
                }
            }
        }
        if (nearestEnd == null) return 0.0;
        return getLinearProgressPercentage(start, nearestEnd, current);
    }
}