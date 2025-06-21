package org.astral.parkour_plugin.timer;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public final class IndividualTimerManager {

    private static final Map<Player, Timer> timers = new HashMap<>();
    private static final Map<Player, Long> persistedStartTimes = new HashMap<>();

    public static void start(final @NotNull Player player, final boolean countdownMode, final int timeLimitSeconds) {
        long now = System.currentTimeMillis();
        persistedStartTimes.put(player, now);
        timers.put(player, new Timer(countdownMode, timeLimitSeconds));
    }

    public static void resume(final @NotNull Player player, final boolean countdownMode, final int timeLimitSeconds) {
        Long persistedTime = persistedStartTimes.get(player);
        if (persistedTime == null) return;

        Timer timer = new Timer(countdownMode, timeLimitSeconds) {
            @Override
            public long getElapsedMillis() {
                return System.currentTimeMillis() - persistedTime;
            }
        };
        timers.put(player, timer);
    }

    public static void stop(final Player player) {
        timers.remove(player);
        persistedStartTimes.remove(player);
    }

    public static Timer get(final Player player) {
        return timers.get(player);
    }

    public static boolean isRunning(final Player player) {
        return timers.containsKey(player);
    }

}