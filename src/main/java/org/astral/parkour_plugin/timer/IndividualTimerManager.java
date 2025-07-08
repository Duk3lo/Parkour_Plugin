package org.astral.parkour_plugin.timer;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class IndividualTimerManager {

    private static final Map<UUID, TimerData> timerDataMap = new HashMap<>();

    public static void start(@NotNull Player player, boolean countdownMode, int timeLimitSeconds) {
        Timer timer = new Timer(countdownMode, timeLimitSeconds, 0L);
        timerDataMap.put(player.getUniqueId(), new TimerData(timer, 0L));
    }

    public static void pause(@NotNull Player player) {
        TimerData data = timerDataMap.get(player.getUniqueId());
        if (data == null) return;
        long elapsed = data.getTimer().getElapsedMillis();
        timerDataMap.put(player.getUniqueId(), new TimerData(data.getTimer(), elapsed));
    }

    public static void resume(@NotNull Player player, boolean countdownMode, int timeLimitSeconds) {
        TimerData data = timerDataMap.get(player.getUniqueId());
        if (data == null) return;
        long elapsed = data.getElapsedMillis();
        Timer resumedTimer = new Timer(countdownMode, timeLimitSeconds, elapsed);
        timerDataMap.put(player.getUniqueId(), new TimerData(resumedTimer, elapsed));
    }

    public static void stop(@NotNull Player player) {
        timerDataMap.remove(player.getUniqueId());
    }

    public static @Nullable Timer get(@NotNull Player player) {
        TimerData data = timerDataMap.get(player.getUniqueId());
        return data != null ? data.getTimer() : null;
    }

    public static boolean isRunning(@NotNull Player player) {
        return timerDataMap.containsKey(player.getUniqueId());
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