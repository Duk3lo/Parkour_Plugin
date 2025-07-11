package org.astral.parkour_plugin.parkour.progress;


import org.astral.parkour_plugin.parkour.checkpoints.Checkpoint;
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

    public double getProgress(Player player, @NotNull List<Checkpoint> checkpoints) {
        int index = getCheckpointIndex(player);
        int totalStages = checkpoints.size();
        if (index < 0 || totalStages == 0) return 0.0;
        double progress = ((index + 1) * 100.0) / totalStages;
        return Math.min(progress, 100.0);
    }

    public @NotNull List<Player> getSortedByProgress(List<Checkpoint> checkpoints) {
        List<Player> players = new ArrayList<>(playerCheckpointMap.keySet());
        players.sort(Comparator.comparingDouble(p -> - getProgress(p, checkpoints)));
        return players;
    }


}
