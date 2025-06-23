package org.astral.parkour_plugin.timer;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public final class IndividualTimerManager {

    private static final Map<Player, TimerData> timerDataMap = new HashMap<>();

    public static void start(final @NotNull Player player, final boolean countdownMode, final int timeLimitSeconds) {
        long now = System.currentTimeMillis();
        Timer timer = new Timer(countdownMode, timeLimitSeconds);
        timerDataMap.put(player, new TimerData(timer, now));
    }

    public static void resume(final @NotNull Player player, final boolean countdownMode, final int timeLimitSeconds) {
        TimerData oldData = timerDataMap.get(player);
        if (oldData == null) return;

        long persistedTime = oldData.getStartTime();
        Timer resumedTimer = new Timer(countdownMode, timeLimitSeconds) {
            @Override
            public long getElapsedMillis() {
                return System.currentTimeMillis() - persistedTime;
            }
        };
        timerDataMap.put(player, new TimerData(resumedTimer, persistedTime));
    }

    public static void stop(final Player player) {
        timerDataMap.remove(player);
    }

    public static @Nullable Timer get(final Player player) {
        TimerData data = timerDataMap.get(player);
        return data != null ? data.getTimer() : null;
    }

    public static boolean isRunning(final Player player) {
        return timerDataMap.containsKey(player);
    }


    private static class TimerData {
        private final Timer timer;
        private final long startTime;

        public TimerData(Timer timer, long startTime) {
            this.timer = timer;
            this.startTime = startTime;
        }

        public Timer getTimer() {
            return timer;
        }

        public long getStartTime() {
            return startTime;
        }
    }
}
