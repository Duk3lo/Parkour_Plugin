package org.astral.parkour_plugin.timer;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class GlobalTimerManager {

    private static final Map<String, Timer> timers = new HashMap<>();
    private static final Map<UUID, String> viewers = new HashMap<>();


    public static void start(@NotNull String map, boolean countdownMode, int timeLimitSeconds) {
        timers.put(map, new Timer(countdownMode, timeLimitSeconds));
    }

    public static boolean isRunning(final @NotNull String map) {
        return timers.containsKey(map);
    }

    @Contract(pure = true)
    public static @NotNull Set<String> getActiveMaps() {
        return timers.keySet();
    }

    public static void stop(final @NotNull String map) {
        timers.remove(map);
        viewers.entrySet().removeIf(entry -> entry.getValue().equals(map));
    }

    public static Timer get(final @NotNull String map) {
        return timers.get(map);
    }

    public static void addViewer(final @NotNull Player player,final @NotNull String map) {
        viewers.put(player.getUniqueId(), map);
    }


    public static void removeViewer(final @NotNull Player player) {
        viewers.remove(player.getUniqueId());
    }

    public static @NotNull Optional<String> getViewingMap(final @NotNull Player player) {
        return Optional.ofNullable(viewers.get(player.getUniqueId()));
    }


    public static @NotNull Set<Player> getViewersOf(final @NotNull String map) {
        Set<Player> result = new HashSet<>();
        for (Map.Entry<UUID, String> entry : viewers.entrySet()) {
            if (entry.getValue().equals(map)) {
                result.add(Bukkit.getPlayer(entry.getValue()));
            }
        }
        return result;
    }

    public static boolean hasAnyTimerRunning() {
        return !timers.isEmpty();
    }
}