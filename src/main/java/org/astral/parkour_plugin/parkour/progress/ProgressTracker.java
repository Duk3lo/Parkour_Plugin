package org.astral.parkour_plugin.parkour.progress;


import org.astral.parkour_plugin.parkour.checkpoints.Checkpoint;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class ProgressTracker {

    private final Map<Player, Integer> playerCheckpointMap = new HashMap<>();

    public void updateCheckpoint(Player player, int index) {
        playerCheckpointMap.put(player, index);
    }

    public void removePlayer(Player player) {
        playerCheckpointMap.remove(player);
    }

    public int getCheckpointIndex(Player player) {
        return playerCheckpointMap.getOrDefault(player, -1);
    }

    public double getProgress(Player player, List<Checkpoint> checkpoints) {
        int index = getCheckpointIndex(player);
        if (index < 0 || index >= checkpoints.size()) return 0.0;
        if (index == checkpoints.size() - 1) return 100.0;

        Location playerLoc = player.getLocation();
        Location current = checkpoints.get(index).getLocation();
        Location next = checkpoints.get(index + 1).getLocation();

        if (current == null || next == null) return (index * 100.0) / (checkpoints.size() - 1);

        double distTotal = current.distance(next);
        double distToNext = playerLoc.distance(next);

        double segmentProgress = 1.0 - (distToNext / distTotal);
        segmentProgress = Math.max(0.0, Math.min(1.0, segmentProgress));

        return ((index + segmentProgress) * 100.0) / (checkpoints.size() - 1);
    }

    public @NotNull List<Player> getSortedByProgress(List<Checkpoint> checkpoints) {
        List<Player> players = new ArrayList<>(playerCheckpointMap.keySet());
        players.sort(Comparator.comparingDouble(p -> - getProgress(p, checkpoints)));
        return players;
    }


}
