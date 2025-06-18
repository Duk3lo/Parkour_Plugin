package org.astral.parkour_plugin.timer;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class IndividualTimerManager {

    private static final Map<Player, Timer> timers = new HashMap<>();
    private static final Map<UUID, Long> persistedStartTimes = new HashMap<>();

    public static void start(@NotNull Player player, boolean countdownMode, int timeLimitSeconds) {
        long now = System.currentTimeMillis();
        persistedStartTimes.put(player.getUniqueId(), now);
        timers.put(player, new Timer(countdownMode, timeLimitSeconds));
    }

    public static void resume(@NotNull Player player, boolean countdownMode, int timeLimitSeconds) {
        Long persistedTime = persistedStartTimes.get(player.getUniqueId());
        if (persistedTime == null) return;

        Timer timer = new Timer(countdownMode, timeLimitSeconds) {
            @Override
            public long getElapsedMillis() {
                return System.currentTimeMillis() - persistedTime;
            }
        };
        timers.put(player, timer);
    }

    public static String stop(Player player) {
        Timer timer = timers.remove(player);
        persistedStartTimes.remove(player.getUniqueId());
        return timer != null ? timer.getFormattedTime() : "00:00.000";
    }

    public static Timer get(Player player) {
        return timers.get(player);
    }

    public static boolean isRunning(Player player) {
        return timers.containsKey(player);
    }

}