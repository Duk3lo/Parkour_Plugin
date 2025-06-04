package org.astral.parkour_plugin.parkour.rankings;

import org.astral.parkour_plugin.parkour.common.Checkpoint;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class Score {
    private final Map<Player, Integer> playerScores = new HashMap<>();
    void updateScore(final Player player, final int checkpointIndex) {
        playerScores.put(player, checkpointIndex);
    }

    public void removePlayerScore(final Player player){
        playerScores.remove(player);
    }

    public @NotNull List<Player> getRankings(final List<Checkpoint> checkpoints, final boolean precision) {
        final List<Map.Entry<Player, Integer>> sortedScores = new ArrayList<>(playerScores.entrySet());

        sortedScores.sort((e1, e2) -> {
            final int comparison = e2.getValue().compareTo(e1.getValue());
            if (precision && comparison == 0) {
                final Player player1 = e1.getKey();
                final Player player2 = e2.getKey();
                final int nextCheckpointIndex1 = e1.getValue() + 1;
                final int nextCheckpointIndex2 = e2.getValue() + 1;

                if (nextCheckpointIndex1 < checkpoints.size() && nextCheckpointIndex2 < checkpoints.size()) {
                    final Location nextCheckpointLoc1 = checkpoints.get(nextCheckpointIndex1).getLocation();
                    final Location nextCheckpointLoc2 = checkpoints.get(nextCheckpointIndex2).getLocation();
                    final double distance1 = calculateDistance(player1.getLocation(), Objects.requireNonNull(nextCheckpointLoc1));
                    final double distance2 = calculateDistance(player2.getLocation(), Objects.requireNonNull(nextCheckpointLoc2));
                    return Double.compare(distance1, distance2);
                }
            }
            return comparison;
        });

        final List<Player> rankings = new ArrayList<>();
        for (final Map.Entry<Player, Integer> entry : sortedScores) {
            rankings.add(entry.getKey());
        }
        return rankings;
    }

    double getCompletionPercentage(final Player player, final List<Checkpoint> checkpoints) {
        if (!playerScores.containsKey(player)) {
            return 0.0;
        }

        final int currentCheckpointIndex = playerScores.get(player);
        if (currentCheckpointIndex < 0 || currentCheckpointIndex >= checkpoints.size() - 1) {
            return 0.0;
        }

        final Checkpoint currentCheckpoint = checkpoints.get(currentCheckpointIndex);
        final Checkpoint nextCheckpoint = checkpoints.get(currentCheckpointIndex + 1);
        final double distanceToNext = calculateDistance(player.getLocation(), Objects.requireNonNull(nextCheckpoint.getLocation()));
        final double totalDistance = calculateDistance(Objects.requireNonNull(currentCheckpoint.getLocation()), nextCheckpoint.getLocation());
        final double percentage = (1.0 - (distanceToNext / totalDistance)) * 100.0;

        return Math.max(0.0, Math.min(100.0, percentage));
    }

    double getOverallCompletionPercentage(final Player player, final List<Checkpoint> checkpoints) {
        if (!playerScores.containsKey(player)) return 0.0;

        int currentCheckpointIndex = playerScores.get(player);
        int totalCheckpoints = checkpoints.size();

        if (totalCheckpoints == 0) return 0.0;
        if (currentCheckpointIndex == totalCheckpoints - 1) return 100.0;

        final Checkpoint currentCheckpoint = checkpoints.get(currentCheckpointIndex);
        final Checkpoint nextCheckpoint = checkpoints.get(currentCheckpointIndex + 1);
        final double distanceToNext = calculateDistance(player.getLocation(), Objects.requireNonNull(nextCheckpoint.getLocation()));
        final double totalDistance = calculateDistance(Objects.requireNonNull(currentCheckpoint.getLocation()), nextCheckpoint.getLocation());
        final double segmentPercentage = (1.0 - (distanceToNext / totalDistance)) * (100.0 / (totalCheckpoints - 1));
        final double overallPercentage = (currentCheckpointIndex * (100.0 / (totalCheckpoints - 1))) + segmentPercentage;

        return Math.max(0.0, Math.min(100.0, overallPercentage));
    }

    private double calculateDistance(final Location loc1, final Location loc2) {
        final double x1 = loc1.getX();
        final double y1 = loc1.getY();
        final double z1 = loc1.getZ();
        final double x2 = loc2.getX();
        final double y2 = loc2.getY();
        final double z2 = loc2.getZ();
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2) + Math.pow(z2 - z1, 2));
    }
}