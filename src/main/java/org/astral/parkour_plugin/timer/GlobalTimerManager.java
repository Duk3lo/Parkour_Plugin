package org.astral.parkour_plugin.timer;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class GlobalTimerManager {

    private static final Map<String, TimerData> timers = new HashMap<>();
    private static final Map<UUID, String> viewers = new HashMap<>();

    public static void start(@NotNull String map, boolean countdownMode, int timeLimitSeconds) {
        Timer timer = new Timer(countdownMode, timeLimitSeconds, 0L);
        timers.put(map, new TimerData(timer, 0L));
    }

    public static void pause(@NotNull String map) {
        TimerData data = timers.get(map);
        if (data == null) return;
        long elapsed = data.getTimer().getElapsedMillis();
        timers.put(map, new TimerData(data.getTimer(), elapsed));
    }

    public static void resume(@NotNull String map, boolean countdownMode, int timeLimitSeconds) {
        TimerData data = timers.get(map);
        if (data == null) return;
        long elapsed = data.getElapsedMillis();
        Timer resumedTimer = new Timer(countdownMode, timeLimitSeconds, elapsed);
        timers.put(map, new TimerData(resumedTimer, elapsed));
    }

    public static boolean isRunning(@NotNull String map) {
        return timers.containsKey(map);
    }

    @Contract(pure = true)
    public static @NotNull Set<String> getActiveMaps() {
        return timers.keySet();
    }

    public static void stop(@NotNull String map) {
        timers.remove(map);
        viewers.entrySet().removeIf(entry -> entry.getValue().equals(map));
    }

    public static Timer get(@NotNull String map) {
        TimerData data = timers.get(map);
        return data != null ? data.getTimer() : null;
    }

    public static void addViewer(@NotNull Player player, @NotNull String map) {
        viewers.put(player.getUniqueId(), map);
    }

    public static void removeViewer(@NotNull Player player) {
        viewers.remove(player.getUniqueId());
    }

    public static @NotNull Optional<String> getViewingMap(@NotNull Player player) {
        return Optional.ofNullable(viewers.get(player.getUniqueId()));
    }

    public static @NotNull Set<Player> getViewersOf(@NotNull String map) {
        Set<Player> result = new HashSet<>();
        for (Map.Entry<UUID, String> entry : viewers.entrySet()) {
            if (entry.getValue().equals(map)) {
                Player player = Bukkit.getPlayer(entry.getKey());
                if (player != null && player.isOnline()) {
                    result.add(player);
                }
            }
        }
        return result;
    }

    public static boolean hasAnyTimerRunning() {
        return !timers.isEmpty();
    }

    private static class TimerData {
        private final Timer timer;
        private final long elapsedMillis;

        public TimerData(Timer timer, long elapsedMillis) {
            this.timer = timer;
            this.elapsedMillis = elapsedMillis;
        }

        public Timer getTimer() {
            return timer;
        }

        public long getElapsedMillis() {
            return elapsedMillis;
        }
    }
}
