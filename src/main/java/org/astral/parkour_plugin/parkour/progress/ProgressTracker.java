package org.astral.parkour_plugin.parkour.progress;


import org.astral.parkour_plugin.parkour.Type.Type;
import org.astral.parkour_plugin.parkour.checkpoints.Checkpoint;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public final class ProgressTracker {

    private final Map<UUID, Integer> playerCheckpointMap = new HashMap<>();

    public void updateCheckpoint(UUID uuid, int index) {
        playerCheckpointMap.put(uuid, index);
    }

    public void removePlayer(@NotNull UUID uuid) {
        playerCheckpointMap.remove(uuid);
    }

    public int getCheckpointIndex(@NotNull UUID uuid) {
        return playerCheckpointMap.getOrDefault(uuid, -1);
    }

    public double getProgress(UUID uuid, @NotNull List<Checkpoint> checkpoints) {
        int index = getCheckpointIndex(uuid);
        int totalStages = checkpoints.size();
        if (index < 0 || totalStages == 0) return 0.0;
        double progress = ((index + 1) * 100.0) / totalStages;
        return Math.min(progress, 100.0);
    }

    public double getCheckpointCompletionPercentage(UUID playerUuid, Collection<Checkpoint> allCheckpoints) {
        if (allCheckpoints == null || allCheckpoints.isEmpty()) return 0.0;
        long checkpointsCompleted = allCheckpoints.stream()
                .filter(checkpoint -> checkpoint.getPlayers().contains(playerUuid))
                .count();
        return ((double) checkpointsCompleted / allCheckpoints.size()) * 100.0;
    }

    public @NotNull List<String> getSortedByProgressCheckpoint(@NotNull List<Checkpoint> checkpoints) {
        List<Checkpoint> globalCheckpoints = checkpoints.stream()
                .filter(cp -> cp.getType() == Type.GLOBAL)
                .collect(Collectors.toList());

        return playerCheckpointMap.keySet().stream()
                .filter(uuid -> globalCheckpoints.stream().anyMatch(cp -> cp.getPlayers().contains(uuid)))
                .sorted(Comparator.comparingDouble(uuid -> -getProgress(uuid, globalCheckpoints)))
                .map(uuid -> {
                    Player onlinePlayer = Bukkit.getPlayer(uuid);
                    return onlinePlayer != null
                            ? onlinePlayer.getName()
                            : Bukkit.getOfflinePlayer(uuid).getName();
                })
                .collect(Collectors.toList());
    }
}