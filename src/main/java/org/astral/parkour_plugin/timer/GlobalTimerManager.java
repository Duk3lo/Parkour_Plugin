package org.astral.parkour_plugin.timer;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class GlobalTimerManager {

    private static final Map<String, Timer> timers = new HashMap<>();

    public static void start(@NotNull String map, boolean countdownMode, int timeLimitSeconds) {
        timers.put(map, new Timer(countdownMode, timeLimitSeconds));
    }

    public static boolean isRunning(@NotNull String map) {
        return timers.containsKey(map);
    }

    @Contract(pure = true)
    public static @NotNull Set<String> getActiveMaps() {
        return timers.keySet();
    }

    public static String stop(@NotNull String map) {
        Timer timer = timers.remove(map);
        return timer != null ? timer.getFormattedTime() : "00:00.000";
    }

    public static Timer get(@NotNull String map) {
        return timers.get(map);
    }

    public static boolean hasAnyTimerRunning() {
        return !timers.isEmpty();
    }
}